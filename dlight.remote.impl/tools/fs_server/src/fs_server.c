
#include "fs_common.h"
#include "fs_server.h"
#include "blocking_queue.h"
#include "exitcodes.h"
#include "dirtab.h"
#include "array.h"

#include <pthread.h>
#include <stddef.h>
#include <dirent.h> 
#include <limits.h> 
#include <stdio.h> 
#include <stdarg.h>
#include <string.h>
#include <ctype.h> 
#include <sys/stat.h>
#include <sys/types.h>
#include <grp.h>
#include <pwd.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <getopt.h>
#include <sys/stat.h> 
#include <fcntl.h>

#define MAX_RP_THREADS 32
static int rp_thread_count = 4;
static pthread_t rp_threads[MAX_RP_THREADS];
static pthread_t refresh_thread;

static blocking_queue req_queue;

static bool log_flag = false;
static bool persistence = false;
static bool refresh = false;
static bool statistics = false;
static int refresh_sleep = 1;

#define FS_SERVER_MAJOR_VERSION 1
#define FS_SERVER_MINOR_VERSION 7

typedef struct fs_entry {
    int /*short?*/ name_len;
    char* name;
    int /*short?*/ link_len;
    char* link;
    unsigned int  uid;
    unsigned int  gid;
    unsigned int mode;
    long size;
    long long mtime;
    char data[];
} fs_entry;

static struct {
    pthread_mutex_t mutex;
    bool proceed;
} state;

static bool state_get_proceed() {    
    bool proceed;
    mutex_lock(&state.mutex);
    proceed = state.proceed;
    mutex_unlock(&state.mutex);    
    return proceed;
}

static void state_set_proceed(bool proceed) {
    mutex_lock(&state.mutex);
    state.proceed = proceed;
    mutex_unlock(&state.mutex);    
}

static void state_init() {
    pthread_mutex_init(&state.mutex, NULL);
    state_set_proceed(true);
}

#define DECLARE_DECODE(type, type_name, maxlen) \
static const char* decode_##type_name (const char* text, type* result) { \
    *result = 0; \
    const char* p = text; \
    if (!isdigit(*p)) { \
        report_error("unexpected numeric value: '%c'\n", *p); \
        return NULL; \
    } \
    while (p - text < maxlen) { \
        char c = *(p++); \
        if (isdigit(c)) { \
            *result *= 10; \
            *result += c - '0'; \
        } else if (c == 0 || isspace(c)) { \
            return p; \
        } else { \
            report_error("unexpected numeric value: '%c'\n", c); \
            return NULL; \
        } \
    } \
    report_error("numeric value too long: '%s'\n", text); \
    return NULL; \
}

DECLARE_DECODE(int, int, 12)
DECLARE_DECODE(unsigned int, uint, 12)
DECLARE_DECODE(long, long, 20)
DECLARE_DECODE(long long, long_long, 20)

static bool is_prohibited(const char* abspath) {
    if (strcmp("/proc", abspath) == 0) {
        return true;
    } else if(strcmp("/dev", abspath) == 0) {
        return true;
    }
    #if linux
    if (strcmp("/run", abspath) == 0) {
        return true;
    }
    #endif
    return false;
}

/** 
 * Decodes in-place fs_raw_request into fs_request
 */
static fs_request* decode_request(char* raw_request, fs_request* request, int request_size) {
    const char* p = raw_request + 2;
    //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
    //p++;
    int id;
    p = decode_int(p, &id);
    if (p == NULL) {
        return NULL;
    }
    //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
    int len;
    p = decode_int(p, &len);
    if (p == NULL) {
        return NULL;
    }   
    if (!len && *raw_request != FS_REQ_QUIT) {
        report_error("wrong (zero path) request: %s", raw_request);
        return NULL;
    }
    if (len > (request_size - sizeof(fs_request) - 1)) {
        report_error("wrong (too long path) request: %s", raw_request);
        return NULL;
    }
    //fs_request->kind = request->kind;
    //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
    request->kind = raw_request[0];
    strncpy(request->path, p, len);
    request->path[len] = 0;
    unescape_strcpy(request->path, request->path);
    len = strlen(request->path);
    request->id = id;
    request->len = len;
    request->size = offsetof(fs_request, path)+len+1; //(request->path-&request)+len+1;
    return request;
}

static fs_entry* create_fs_entry(fs_entry *entry2clone) {
    int sz = sizeof(fs_entry) + entry2clone->name_len + entry2clone->link_len + 2;
    fs_entry *entry = malloc(sz);
    entry->name_len = entry2clone->name_len;
    entry->name = entry->data;
    strncpy(entry->name, entry2clone->name, entry->name_len);
    entry->name[entry->name_len] = 0;
    entry->link_len = entry2clone->link_len;
    if (entry->link_len) {
        entry->link = entry->data + entry->name_len + 1;
        strncpy((char*)entry->link, entry2clone->link, entry->link_len);
        entry->link[entry->link_len] = 0;
    } else {
        entry->link = "";
    }
    entry->gid = entry2clone->gid;
    entry->uid = entry2clone->uid;
    entry->mode = entry2clone->mode;
    entry->size = entry2clone->size;
    entry->mtime = entry2clone->mtime;
    return entry;
}


/** 
 * Creates a fs_entry on heap.
 * NB: modifies buf: can unescape and zero-terminate strings
 */
static fs_entry *decode_entry_response(char* buf) {

    // format: name_len name uid gid mode size mtime link_len link
    fs_entry tmp; // a temporary one since we don't know names size

    const char* p = decode_int(buf, &tmp.name_len);
    if (!p) { return NULL; }; // decode_int already printed error message
    
    tmp.name = (char*) p;
    tmp.name[tmp.name_len] = 0;
    unescape_strcpy(tmp.name, tmp.name);
    p += tmp.name_len + 1;
    tmp.name_len = strlen(tmp.name);

    p = decode_uint(p, &tmp.uid);
    if (!p) { return NULL; }; // decode_int already printed error message
    
    p = decode_uint(p, &tmp.gid);
    if (!p) { return NULL; };
    
    p = decode_uint(p, &tmp.mode);
    if (!p) { return NULL; };
    
    p = decode_long(p, &tmp.size);
    if (!p) { return NULL; };
    
    p = decode_long_long(p, &tmp.mtime);
    if (!p) { return NULL; };
    
    p = decode_int(p, &tmp.link_len);
    if (!p) { return NULL; };

    if (tmp.link_len) {
        tmp.link = (char*) p;
        tmp.link[tmp.link_len] = 0;
        unescape_strcpy(tmp.link, tmp.link);
        tmp.link_len = strlen(tmp.link);
    } else {
        tmp.link = "";
    }
    if (tmp.name_len > MAXNAMLEN) {
        report_error("wrong entry format: too long (%i) file name: %s", tmp.name_len, buf);
        return NULL;
    }
    if (tmp.link_len > PATH_MAX) {
        report_error("wrong entry format: too long (%i) link name: %s", tmp.link_len, buf);
        return NULL;
    }
    return create_fs_entry(&tmp);
}

static void read_entries_from_cache(array/*<fs_entry>*/ *entries, FILE *cache_fp, const char* path) {
    array_init(entries, 100);
    if (cache_fp) {
        int buf_size = PATH_MAX + 40;
        char *buf = malloc(buf_size);
        if (!fgets(buf, buf_size, cache_fp)) {
            report_error("error reading cache for %s: %s\n", path, strerror(errno));
        }
        unescape_strcpy(buf, buf);
        if (strncmp(path, buf, strlen(path)) != 0) {
            report_error("error: first line in cache for %s is not '%s', but is '%s'", path, path, buf);
        }
        while (fgets(buf, buf_size, cache_fp)) {
            trace("\tread entry: %s", buf);
            if (*buf == '\n' || *buf == 0) {
                trace("an empty one; continuing...");
                continue;
            }
            fs_entry *entry = decode_entry_response(buf);
            if (entry) {
                array_add(entries, entry);
            } else {
                report_error("error reading entry from cache: %s\n", buf);
            }
        }
        if (!feof(cache_fp)) {
            report_error("error reading cache for %s: %s\n", path, strerror(errno));
        }
        free(buf);
        // do not close cache_fp, it's caller's responsibility
    }
    array_truncate(entries);
}

static bool visit_dir_entries(const char* path, 
        bool (*visitor) (char* name, struct stat *st, char* link, const char* abspath, void *data), void *data) {
    DIR *d = d = opendir(path);
    if (d) {
        union {
            struct dirent d;
            char b[MAXNAMLEN];
        } entry_buf;
        entry_buf.d.d_reclen = MAXNAMLEN + sizeof (struct dirent);
        int buf_size = PATH_MAX;
        char *abspath = malloc(buf_size);
        char *link = malloc(buf_size);
        // TODO: error processing (malloc() can return null)
        int base_len = strlen(path);
        strcpy(abspath, path);
        abspath[base_len] = '/';
        struct dirent *entry;
        while (true) {
            if (readdir_r(d, &entry_buf.d, &entry)) {
                report_error("error reading directory %s: %s\n", path, strerror(errno));
                break;
            }
            if (!entry) {
                break;
            }
            if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
                continue;
            }
            strcpy(abspath + base_len + 1, entry->d_name);
            struct stat stat_buf;
            if (lstat(abspath, &stat_buf) == 0) {
                bool is_link = S_ISLNK(stat_buf.st_mode);
                if (is_link) {
                    ssize_t sz = readlink(abspath, link, buf_size);
                    if (sz == -1) {
                        report_error("error performing readlink for %s: %s\n", abspath, strerror(errno));
                        strcpy(link, "?");
                    } else {
                        link[sz] = 0;
                    }
                }
                if (!visitor(entry->d_name, &stat_buf, link, abspath, data)) {
                    break;
                }
            } else {
                report_error("error getting stat for '%s': %s\n", abspath, strerror(errno));                
            }
        }
        free(abspath);
        free(link);
        closedir(d);
        return true; // TODO: error processing: what some of them has errors?
    } else {
        report_error("error opening directory '%s': %s\n", path, strerror(errno));
        return false;
    }
}

static long long get_mtime(struct stat *stat_buf) {
    long long  result = stat_buf->st_mtime;
    result *= 1000;
#if __FreeBSD__
    #if __BSD_VISIBLE
        result += stat_buf->st_mtimespec.tv_nsec/1000000;
    #else
        result += stat_buf->__st_mtimensec/1000000;
    #endif
#elif __APPLE__
    result += stat_buf->st_mtimespec.tv_nsec/1000000;
#else
    result +=  stat_buf->st_mtim.tv_nsec/1000000;    
#endif
    return result;
}

static bool fs_entry_creating_visitor(char* name, struct stat *stat_buf, char* link, const char* abspath, void *data) {
    fs_entry tmp;
    tmp.name_len = strlen(name);
    tmp.name = name;
    tmp.uid = stat_buf->st_uid;
    tmp.gid = stat_buf->st_gid;
    tmp.mode = stat_buf->st_mode;
    tmp.size = stat_buf->st_size;
    tmp.mtime = get_mtime(stat_buf);
    bool is_link = S_ISLNK(stat_buf->st_mode);
    tmp.link_len = is_link ? strlen(link) : 0;
    tmp.link = is_link ? link : "";
    fs_entry* new_entry = create_fs_entry(&tmp);
    if (new_entry) {
        array_add((array*)data, new_entry);
    } else {
        report_error("error creating entry for %s\n", abspath);
    }
    return true;
}

static void read_entries_from_dir(array/*<fs_entry>*/ *entries, const char* path) {
    array_init(entries, 100);
    visit_dir_entries(path, fs_entry_creating_visitor, entries);
    array_truncate(entries);
}

static bool form_entry_response(char* response_buf, const int response_buf_size, 
        const char *abspath, const struct dirent *entry, 
        char* work_buf, int work_buf_size) {
    struct stat stat_buf;
    if (lstat(abspath, &stat_buf) == 0) {
        
        //int escaped_name_size = escape_strlen(entry->d_name);
        escape_strcpy(work_buf, entry->d_name);
        char *escaped_name = work_buf;
        int escaped_name_size = strlen(escaped_name);
        work_buf_size -= (escaped_name_size + 1);
        
        bool link_flag = S_ISLNK(stat_buf.st_mode);

        int escaped_link_size = 0;
        char* escaped_link = "";
        
        if (link_flag) {
            char* link = work_buf + escaped_name_size + 1; 
            ssize_t sz = readlink(abspath, link, work_buf_size);
            if (sz == -1) {
                report_error("error performing readlink for %s: %s\n", abspath, strerror(errno));
                strcpy(work_buf, "?");
            } else {
                link[sz] = 0;
                escaped_link_size = escape_strlen(link);
                work_buf_size -= (sz + escaped_link_size + 1);
                if (work_buf_size < 0) {
                    report_error("insufficient space in buffer for %s\n", abspath);
                    return false;
                }
                escaped_link = link + sz + 1;
                escape_strcpy(escaped_link, link);
            }
        }
        snprintf(response_buf, response_buf_size, "%i %s %li %li %li %lu %lli %i %s\n",
                escaped_name_size,
                escaped_name,
                (long) stat_buf.st_uid,
                (long) stat_buf.st_gid,
                (long) stat_buf.st_mode,
                (unsigned long) stat_buf.st_size,
                get_mtime(&stat_buf),
                escaped_link_size,
                escaped_link);
        return true;
    } else {
        report_error("error getting stat for '%s': %s\n", abspath, strerror(errno));
        return false;
    }
}

static void response_ls(int request_id, const char* path, bool recursive, int nesting_level) {

    if (is_prohibited(path)) {
        trace("ls: skipping %s\n", path);
        return;
    }
    
    DIR *d = NULL;
    FILE *cache_fp = NULL;
    struct dirent *entry;
    
    union {
        struct dirent d;
        char b[MAXNAMLEN];
    } entry_buf;    
    entry_buf.d.d_reclen = MAXNAMLEN + sizeof(struct dirent);
    
    int response_buf_size = PATH_MAX * 2; // TODO: accurate size calculation
    char* response_buf = malloc(response_buf_size); 
    char* child_abspath = malloc(PATH_MAX);
    int work_buf_size = (PATH_MAX + MAXNAMLEN) * 2 + 2;
    char* work_buf = malloc(work_buf_size);
    d = opendir(path);
    if (d) {
        dirtab_element *el = NULL;
        if (persistence) {
            el = dirtab_get_element(path);
            cache_fp = dirtab_get_element_cache(el, true);
            if (!cache_fp) {
                report_error("error opening cache file for %s: %s\n", path, strerror(errno));
            }
            escape_strcpy(work_buf, path);
            fprintf(cache_fp, "%s\n", work_buf);
        }        
        int cnt = 0;
        while (true) {
            if (readdir_r(d, &entry_buf.d, &entry)) {
                report_error("error reading directory %s: %s\n", path, strerror(errno));
                break;
            }
            if (!entry) {
                break;
            }
            if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
                continue;
            }
            // see comment on NFS entries below
            if (strchr(entry->d_name, '/')) {
                continue;
            }
            cnt++;
        }
        rewinddir(d);
        fprintf(stdout, "%c %d %li %s %d\n", (recursive ? FS_RSP_RECURSIVE_LS : FS_RSP_LS),
                request_id, (long) strlen(path), path, cnt);
        int base_len = strlen(path);
        strcpy(child_abspath, path);
        child_abspath[base_len] = '/';
        while (true) {
            if (readdir_r(d, &entry_buf.d, &entry)) {
                report_error("error reading directory %s: %s\n", path, strerror(errno));
                break;
            }
            if (!entry) {
                break;
            }
            if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
                continue;
            }
            //trace("\tentry: '%s'\n", entry->d_name);            
            // on NFS entry->d_name may contain '/' or even be absolute!
            // for example, "/ws" directory can contain 
            // "bb-11u1", /ws/bb-11u1/packages" and "bb-11u1/packages" entries!
            // TODO: investigate how to process this properly
            // for now just ignoring such entries
            if (strchr(entry->d_name, '/')) {
                report_error("skipping entry %s\n", entry->d_name);
                continue;
            }
            strcpy(child_abspath + base_len + 1, entry->d_name);
            if (form_entry_response(response_buf, response_buf_size, child_abspath, entry, work_buf, work_buf_size)) {
                fprintf(stdout, "%c %d %s", FS_RSP_ENTRY, request_id, response_buf);
                if (cache_fp) {
                    fprintf(cache_fp, "%s",response_buf); // trailing '\n' already there, added by form_entry_response
                }
            } else {
                report_error("error forming entry response for '%s'\n", child_abspath);
            }
        }
        fprintf(stdout, "%c %d %li %s\n", FS_RSP_END, request_id, (long) strlen(path), path);
        fflush(stdout);
        if (recursive) {
            rewinddir(d);
            while (true) {
                if (readdir_r(d, &entry_buf.d, &entry)) {
                    report_error("error reading directory %s: %s\n", path, strerror(errno));
                    break;
                }
                if (!entry) {
                    break;
                }
                if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
                    continue;
                }
                strcpy(child_abspath + base_len + 1, entry->d_name);
                struct stat stat_buf;
                if (lstat(child_abspath, &stat_buf) == 0) {
                    if (S_ISDIR(stat_buf.st_mode)) {
                        response_ls(request_id, child_abspath, true, nesting_level+1);
                    }
                }
            }
            if (nesting_level == 0) {
                fprintf(stdout, "%c %d %li %s\n", FS_RSP_END, request_id, (long) strlen(path), path);
                fflush(stdout);
            }
        }
        if (cache_fp) {
            dirtab_release_element_cache(el);
        }
    } else {
        report_error("error opening directory '%s': %s\n", path, strerror(errno));
    }
    closedir_if_not_null(d);
    free(work_buf);
    free(child_abspath);
    free(response_buf);
}

static void response_stat(int request_id, const char* path) {
    
}

static void process_request(fs_request* request) {
    switch (request->kind) {
        case FS_REQ_LS:
            response_ls(request->id, request->path, false, 0);
            break;
        case FS_REQ_RECURSIVE_LS:
            response_ls(request->id, request->path, true, 0);
            break;
        case FS_REQ_STAT:
            response_stat(request->id, request->path);
        default:
            report_error("unexpected mode: '%c'\n", request->kind);
    }
    //fflush(stdout);
    //fflush(stderr);
}

static int entry_comparator(const void *element1, const void *element2) {
    const fs_entry *e1 = *((fs_entry**) element1);
    const fs_entry *e2 = *((fs_entry**) element2);
    int res = strcmp(e1->name, e2->name);
    return res;
}

static bool refresh_visitor(const char* path, int index, dirtab_element* el) {
    if (is_prohibited(path)) {
        trace("refresh manager: skipping %s\n", path);
        return true;
    }
    trace("refresh manager: visiting %s\n", path);
    
    array/*<fs_entry>*/ old_entries;
    array/*<fs_entry>*/ new_entries;
    FILE* cache_fp = dirtab_get_element_cache(el, false);
    if (cache_fp) {
        read_entries_from_cache(&old_entries, cache_fp, path);
        dirtab_release_element_cache(el);
    } else {
        report_error("error refreshing %s: can't open cache\n", path);
        return true;
    }
    read_entries_from_dir(&new_entries, path);

    array_qsort(&old_entries, entry_comparator);
    array_qsort(&new_entries, entry_comparator);
    
    bool differs = array_size(&new_entries) != array_size(&old_entries);
    if (!differs) {
        for (int i = 0; i < new_entries.size; i++) {
            fs_entry *new_entry = array_get(&new_entries, i);
            fs_entry *old_entry = array_get(&old_entries, i);
            if (new_entry->name_len != old_entry->name_len) {
                differs = true;
                trace("refresh manager: names differ (1) in directory %s: %s vs %s\n", path, new_entry->name, old_entry->name);
                break;
            }
            if (strcmp(new_entry->name, old_entry->name) != 0) {
                differs = true;
                trace("refresh manager: names differ (2) in directory %s: %s vs %s\n", path, new_entry->name, old_entry->name);
                break;
            }
            // names are same; check types (modes))
            if (new_entry->mode != old_entry->mode) {
                differs = true;
                trace("refresh manager: modes differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->mode, old_entry->mode);
                break;
            }
            // if links, then check links
            if (S_ISLNK(new_entry->mode)) {
                if (new_entry->link_len != old_entry->link_len) {
                    differs = true;
                    trace("refresh manager: links differ (1) for %s/%s: %s vs %s\n", path, new_entry->name, new_entry->link, old_entry->link);
                    break;
                }
                if (strcmp(new_entry->link, old_entry->link) != 0) {
                    differs = true;
                    trace("refresh manager: links differ (2) for %s/%s: %s vs %s\n", path, new_entry->name, new_entry->link, old_entry->link);
                    break;
                }                
            }
            // names, modes and link targets are same
            if (new_entry->uid != old_entry->uid) {
                differs = true;
                trace("refresh manager: uids differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->uid, old_entry->uid);
                break;
            }
            if (new_entry->gid != old_entry->gid) {
                differs = true;
                trace("refresh manager: gids differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->gid, old_entry->gid);
                break;
            }
            if (S_ISREG(new_entry->mode)) {
                if (new_entry->size != old_entry->size) {
                    differs = true;
                    trace("refresh manager: sizes differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->size, old_entry->size);
                    break;
                }
                if (new_entry->mtime != old_entry->mtime) {
                    differs = true;
                    trace("refresh manager: times differ for %s/%s: %lld vs %lld\n", path, new_entry->name, new_entry->mtime, old_entry->mtime);
                    break;
                }
            }
        }
    }

    if (differs) {
        trace("refresh manager: sending notification for %s\n", path);
        // trailing '\n' already there, added by form_entry_response
        fprintf(stdout, "%c 0 %li %s\n", FS_RSP_CHANGE, (long) strlen(path), path);
        fflush(stdout);
    }     
    array_free(&old_entries);
    array_free(&new_entries);
    return true;        
}

static void *refresh_loop(void *data) {    
    trace("refresh manager started; sleep interval is %d\n", refresh_sleep);
    int pass = 0;
    while (dirtab_is_empty()) { //TODO: replace with notification?
        sleep(refresh_sleep ? refresh_sleep : 2);
    }
    while (state_get_proceed()) {
        pass++;
        trace("refresh manager, pass %d\n", pass);
        dirtab_flush(); // TODO: find the appropriate place
        stopwatch_start();
        dirtab_visit(refresh_visitor);
        stopwatch_stop("refresh cycle");
        if (refresh_sleep) {
            sleep(refresh_sleep);
        }
    }
    trace("refresh manager stopped\n");
    return NULL;
}

static void *rp_loop(void *data) {
    int thread_num = *((int*) data);
    trace("Thread #%d started\n", thread_num);
    while (true) {
        fs_request* request = blocking_queue_poll(&req_queue);
        if (request) {
            trace("thread[%d] request #%d sz=%d kind=%c len=%d path=%s\n", 
                    thread_num, request->id, request->size, request->kind, request->len, request->path);
            process_request(request);
            free(request);
        } else {
            if (!state_get_proceed()) {
                break;
            }
        }
    }
    trace("Thread #%d done\n", thread_num);
    return NULL;
}

static void lock_or_unloock(bool lock) {
    const char* lock_file_name = "lock";
    static int lock_fd = -1;
    if (lock) {
        lock_fd = open(lock_file_name, O_WRONLY | O_CREAT, 0600);
        if (lock_fd < 0) {
            report_error("error opening lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(errno));
            exit(FAILURE_OPENING_LOCK_FILE);
        }
        if(lockf(lock_fd, F_TLOCK, 0)) {
            report_error("error locking lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(errno));
            exit(FAILURE_LOCKING_LOCK_FILE);
        }
    } else {
        if (lockf(lock_fd, F_ULOCK, 0)) {
            report_error("error unlocking lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(errno));
            exit(FAILURE_LOCKING_LOCK_FILE);
        }
        close(lock_fd);        
    }
}

static void exit_function() {
    dirtab_flush();
    lock_or_unloock(false);    
}

static void main_loop() {
    //TODO: handshake with version    
    
    if (rp_thread_count > 1) {
        blocking_queue_init(&req_queue);
        trace("Staring %d threads\n", rp_thread_count);
        int thread_num[rp_thread_count];
        for (int i = 0; i < rp_thread_count; i++) {
            trace("Starting thread #%d...\n", i);
            thread_num[i] = i;
            pthread_create(&rp_threads[i], NULL, &rp_loop, &thread_num[i]);
        }
    } else {
        trace("Starting in single-thread mode\n");
    }

    if (refresh) {
        pthread_create(&refresh_thread, NULL, &refresh_loop, NULL);
    }
    if (atexit(exit_function)) {
        report_error("error setting exit function: %s\n", strerror(errno));
    }

    int buf_size = 256 + PATH_MAX;
    char *raw_req_buffer = malloc(buf_size);
    char *req_buffer = malloc(buf_size);
    while(fgets(raw_req_buffer, buf_size, stdin)) {
        trace("raw request: %s", raw_req_buffer); // no LF since buffer ends it anyhow 
        log_print(raw_req_buffer);
        fs_request* request = decode_request(raw_req_buffer, (fs_request*) req_buffer, buf_size);
        if (request) {
            trace("decoded request #%d sz=%d kind=%c len=%d path=%s\n", request->id, request->size, request->kind, request->len, request->path);
            if (request->kind == FS_REQ_QUIT) {
                break;
            }
            if (request->kind == FS_REQ_SLEEP) {
                int interval = 0;
                for (int i = 0; i < request->len; i++) {
                    char c = request->path[i];
                    if (isdigit(c)) {
                        interval = (interval*10) + (c - '0');
                    } else {
                        break;
                    }
                }
                if (interval) {
                    fprintf(stderr, "fs_server: sleeping %i seconds\n", interval);
                    sleep(interval);
                    fprintf(stderr, "fs_server: awoke\n");
                }
                continue;
            }
            if (rp_thread_count > 1) {
                fs_request* new_request = malloc(request->size);
                memcpy(new_request, request, request->size);
                blocking_queue_add(&req_queue, new_request);
            } else {
                process_request(request);
            }
       } else {
            report_error("incorrect request: %s", raw_req_buffer);
       }
    }
    free(req_buffer);
    free(raw_req_buffer);
    state_set_proceed(false);
    blocking_queue_shutdown(&req_queue);
    trace("Max. requests queue size: %d\n", blocking_queue_max_size(&req_queue));
    if (statistics) {
        fprintf(stderr, "Max. requests queue size: %d\n", blocking_queue_max_size(&req_queue));
    }
    trace("Shutting down. Joining threads...\n");
    for (int i = 0; i < rp_thread_count; i++) {
        trace("Shutting down. Joining thread #%i [%ui]\n", i, rp_threads[i]);
        pthread_join(rp_threads[i], NULL);
    }
}

static void usage(char* argv[]) {
    char *prog_name = strrchr(argv[0], '/');
    fprintf(stderr, 
            "Usage: %s [-t nthreads] [-v] [-p] [-r]\n"
            "   -t nthreads response processing threads count (default is %d)\n"
            "   -p persisnence\n"
            "   -r nsec  set refresh ON and sets refresh interval in seconds\n"
            "   -v verbose: print trace messages\n"
            "   -l log: log all requests into log file\n"
            "   -s statistics: orint some statistics output to stderr\n"
            , prog_name ? prog_name : argv[0], rp_thread_count);
}

void process_options(int argc, char* argv[]) {
    int opt;
    int new_thread_count, new_refresh_sleep;
    while ((opt = getopt(argc, argv, "r:pvt:ls")) != -1) {
        switch (opt) {
            case 's':
                statistics = true;
                break;
            case 'r':
                refresh  = true;
                new_refresh_sleep = atoi(optarg);
                if (new_refresh_sleep >= 0) {
                    refresh_sleep = new_refresh_sleep;
                }
                break;
            case 'l':
                log_flag = true;
                break;
            case 'p':
                persistence  = true;
                break;
            case 'v':
                set_trace(true);
                break;
            case 't':
                new_thread_count = atoi(optarg);
                if (new_thread_count > 0) {
                    if (new_thread_count > MAX_RP_THREADS) {
                        report_error("incorrect value of -t flag: %d. Should not exceed %d.\n", new_thread_count, MAX_RP_THREADS);
                        rp_thread_count = MAX_RP_THREADS;
                    } else {
                        rp_thread_count = new_thread_count;
                    }
                }
                break;
            default: /* '?' */
                usage(argv);
                exit(WRONG_ARGUMENT);
                break;
        }
    }
    if (refresh && !persistence) {
        report_error("incorrect parameters combination: refresh without persistence does not work\n");
        usage(argv);
        exit(WRONG_ARGUMENT);
    }
}

static bool print_visitor(const char* path, int index, dirtab_element* el) {
    trace("%d %s\n", index, path);
    return true;
}

int main(int argc, char* argv[]) {
    process_options(argc, argv);
    trace("Version %d.%d (%s %s)\n", FS_SERVER_MAJOR_VERSION, FS_SERVER_MINOR_VERSION, __DATE__, __TIME__);
    dirtab_init();
    const char* basedir = dirtab_get_basedir();
    if (chdir(basedir)) {
        report_error("cannot change current directory to %s: %s\n", basedir, strerror(errno));
        exit(FAILED_CHDIR);
    }
    lock_or_unloock(true);
    state_init();
    if (get_trace() && ! dirtab_is_empty()) {
        trace("loaded dirtab:\n");
        dirtab_visit(print_visitor);
    }
    if (log_flag) {
       log_open("log") ;
       log_print("\n--------------------------------------\nfs_server started  ");
       time_t t = time(NULL);
       struct tm *tt = localtime(&t);
       if (tt) {
           log_print("%d/%02d/%02d %02d:%02d:%02d\n", 
                   tt->tm_year+1900, tt->tm_mon + 1, tt->tm_mday, 
                   tt->tm_hour, tt->tm_min, tt->tm_sec);
       } else {
           log_print("<error getting time: %s>\n", strerror(errno));
       }       
       for (int i = 0; i < argc; i++) {
           log_print("%s ", argv[i]);
       }
       log_print("\n");
    }
    main_loop();
    if (!dirtab_flush()) {
        report_error("error storing dirtab\n");
    }
    dirtab_free();
    log_close();
    return 0;
}
