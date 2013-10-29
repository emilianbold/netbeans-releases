
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

static bool persistence = false;
static bool refresh = false;
static int refresh_sleep = 1;

#define FS_SERVER_MAJOR_VERSION 1
#define FS_SERVER_MINOR_VERSION 3

typedef struct fs_entry {
    int /*short?*/ name_len;
    char* name;
    int /*short?*/ link_len;
    char* link;
    unsigned int  uid;
    unsigned int  gid;
    unsigned int mode;
    long size;
    long mtime;
    char data[];
} fs_entry;

static const char* decode_int(const char* text, int* result) {
    *result = 0;
    const char* p = text;
    while (p - text < 12) {
        char c = *(p++);
        if (isdigit(c)) {
            *result *= 10; 
            *result += c - '0';
        } else if (c == 0 || isspace(c)) {
            return p;
        }
    }
    report_error("unexpected numeric value: '%s'\n", text);
    return NULL;
}

static const char* decode_uint(const char* text, unsigned int* result) {
    *result = 0;
    const char* p = text;
    while (p - text < 12) {
        char c = *(p++);
        if (isdigit(c)) {
            *result *= 10; 
            *result += c - '0';
        } else if (c == 0 || isspace(c)) {
            return p;
        }
    }
    report_error("unexpected numeric value: '%s'\n", text);
    return NULL;
}

static const char* decode_long(const char* text, long* result) {
    *result = 0;
    const char* p = text;
    while (p - text < 24) {
        char c = *(p++);
        if (isdigit(c)) {
            *result *= 10; 
            *result += c - '0';
        } else if (c == 0 || isspace(c)) {
            return p;
        }
    }
    report_error("unexpected numeric value: '%s'\n", text);
    return NULL;
}

static bool is_prohibited(const char* abspath) {
    return strcmp("/proc", abspath) == 0 || strcmp("/dev", abspath) == 0;
}

/** 
 * Decodes in-place fs_raw_request into fs_request
 */
static fs_request* decode_request(char* raw_request, fs_request* request) {
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
    //fs_request->kind = request->kind;
    //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
    request->kind = raw_request[0];
    strncpy(request->path, p, len);
    request->path[len] = 0;
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


/** allocates fs_entry on heap */
static fs_entry *decode_entry_response(const char* buf) {
    // format: name_len name uid gid mode size mtime link_len link
    fs_entry tmp; // a temporary one since we don't know names size
    const char* p = decode_int(buf, &tmp.name_len);
    tmp.name = (char*) p;
    p += tmp.name_len + 1;
    p = decode_uint(p, &tmp.uid);
    p = decode_uint(p, &tmp.gid);
    p = decode_uint(p, &tmp.mode);
    p = decode_long(p, &tmp.size);
    p = decode_long(p, &tmp.mtime);
    p = decode_int(p, &tmp.link_len);
    tmp.link = (char*) p;
    return create_fs_entry(&tmp);
}

static void read_entries_from_cache(array/*<fs_entry>*/ *entries, const char* cache, const char* path) {
    array_init(entries, 100);
    FILE *f = NULL;
    f = fopen(cache, "r");
    if (f) {
        int buf_size = PATH_MAX + 40;
        char *buf = malloc(buf_size);
        if (!fgets(buf, buf_size, f)) {
            report_error("error reading cache'%s/%s': %s\n", dirtab_get_basedir(), cache, strerror(errno));
        }
        if (strncmp(path, buf, strlen(path)) != 0) {
            report_error("error: first line in file '%s/%s' is not '%s', but is '%s'", dirtab_get_basedir(), cache, path, buf);
        }
        while (fgets(buf, buf_size, f)) {
            fs_entry *entry = decode_entry_response(buf);
            array_add(entries, entry);
        }
        if (!feof(f)) {
            report_error("error reading '%s/%s': %s\n", dirtab_get_basedir(), cache, strerror(errno));
        }
        free(buf);
        fclose(f);
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
        char *abspath = malloc(PATH_MAX);
        char *link = malloc(PATH_MAX);
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
                    ssize_t sz = readlink(abspath, link, sizeof link);
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

static bool fs_entry_creating_visitor(char* name, struct stat *stat_buf, char* link, const char* abspath, void *data) {
    fs_entry tmp;
    tmp.name_len = strlen(name);
    tmp.name = name;
    tmp.uid = stat_buf->st_uid;
    tmp.gid = stat_buf->st_gid;
    tmp.mode = stat_buf->st_mode;
    tmp.size = stat_buf->st_size;
    tmp.mtime = stat_buf->st_mtime;
    bool is_link = S_ISLNK(stat_buf->st_mode);
    tmp.link_len = is_link ? strlen(link) : 0;
    tmp.link = is_link ? link : "";
    array_add((array*)data, create_fs_entry(&tmp));
    return true;
}

static void read_entries_from_dir(array/*<fs_entry>*/ *entries, const char* path) {
    array_init(entries, 100);
    visit_dir_entries(path, fs_entry_creating_visitor, entries);
    array_truncate(entries);
}

static bool form_entry_response(char* buf, const int buf_size, const char *abspath, const struct dirent *entry) {
    struct stat stat_buf;
    if (lstat(abspath, &stat_buf) == 0) {
        char link[PATH_MAX];
        bool link_flag = S_ISLNK(stat_buf.st_mode);
        if (link_flag) {
            ssize_t sz = readlink(abspath, link, sizeof link);
            if (sz == -1) {
                report_error("error performing readlink for %s: %s\n", abspath, strerror(errno));
                strcpy(link, "?");
            } else {
                link[sz] = 0;
            }
        }
        snprintf(buf, buf_size, "%li %s %li %li %li %li %li %li %s\n",
                (long) strlen(entry->d_name),
                entry->d_name,
                (long) stat_buf.st_uid,
                (long) stat_buf.st_gid,
                (long) stat_buf.st_mode,
                (long) stat_buf.st_size,
                (long) stat_buf.st_mtime,
                (long) (link_flag ? strlen(link) : 0),
                (link_flag ? link : ""));
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
    FILE *f = NULL;
    struct dirent *entry;
    
    union {
        struct dirent d;
        char b[MAXNAMLEN];
    } entry_buf;    
    entry_buf.d.d_reclen = MAXNAMLEN + sizeof(struct dirent);
    
    int buf_size = PATH_MAX * 2; // TODO: accurate size calculation
    char* buf = malloc(buf_size); 
    d = opendir(path);
    if (d) {
        if (persistence) {
            const char *cache = dirtab_get_cache(path);
            f = fopen(cache, "w");
            if (!f) {
                report_error("error opening file %s: %s\n", cache, strerror(errno));
            }
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
            cnt++;
        }
        rewinddir(d);
        fprintf(stdout, "%c %d %li %s %d\n", (recursive ? FS_RSP_RECURSIVE_LS : FS_RSP_LS),
                request_id, (long) strlen(path), path, cnt);
        if (f) {
            fprintf(f, "%s\n", path);
        }
        char abspath[PATH_MAX];
        int base_len = strlen(path);
        strcpy(abspath, path);
        abspath[base_len] = '/';
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
            if (form_entry_response(buf, buf_size, abspath, entry)) {
                fprintf(stdout, "%c %d %s", FS_RSP_ENTRY, request_id, buf);
                if (f) {
                    fprintf(f, "%s",buf); // trailing '\n' already there, added by form_entry_response
                }
            } else {
                report_error("error getting stat for '%s': %s\n", abspath, strerror(errno));
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
                strcpy(abspath + base_len + 1, entry->d_name);
                struct stat stat_buf;
                if (lstat(abspath, &stat_buf) == 0) {
                    if (S_ISDIR(stat_buf.st_mode)) {
                        response_ls(request_id, abspath, true, nesting_level+1);
                    }
                }
            }
            if (nesting_level == 0) {
                fprintf(stdout, "%c %d %li %s\n", FS_RSP_END, request_id, (long) strlen(path), path);
                fflush(stdout);
            }
        }                
    } else {
        report_error("error opening directory '%s': %s\n", path, strerror(errno));
    }
    fclose_if_not_null(f);
    closedir_if_not_null(d);
    free(buf);
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

static bool refresh_visitor(const char* path, int index, const char* cache) {
    if (is_prohibited(path)) {
        trace("refresh manager: skipping %s\n", path);
        return true;
    }
    trace("refresh manager: visiting %s\n", path);
    
    array/*<fs_entry>*/ old_entries;
    array/*<fs_entry>*/ new_entries;
    read_entries_from_cache(&old_entries, cache, path);
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
                    trace("refresh manager: times differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->mtime, old_entry->mtime);
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
    while (true) {
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
    
    trace("Version %d.%d\n", FS_SERVER_MAJOR_VERSION, FS_SERVER_MINOR_VERSION);
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

    char raw_req_buffer[256 + PATH_MAX];
    char req_buffer[256 + PATH_MAX];
    while(fgets(raw_req_buffer, sizeof raw_req_buffer, stdin)) {
        trace("raw request: %s", raw_req_buffer); // no LF since buffer ends it anyhow 
        fs_request* request = decode_request(raw_req_buffer, (fs_request*) req_buffer);
        trace("decoded request #%d sz=%d kind=%c len=%d path=%s\n", request->id, request->size, request->kind, request->len, request->path);
        if (request) {
            if (request->kind == FS_REQ_QUIT) {
                break;
            }
            if (rp_thread_count > 1) {
                fs_request* new_request = malloc(request->size);
                memcpy(new_request, request, request->size);
                blocking_queue_add(&req_queue, new_request);
            } else {
                process_request(request);
            }
       }
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
            , prog_name ? prog_name : argv[0], rp_thread_count);
}

void process_options(int argc, char* argv[]) {
    int opt;
    int new_thread_count, new_refresh_sleep;
    while ((opt = getopt(argc, argv, "r:pvt:")) != -1) {
        switch (opt) {
            case 'r':
                refresh  = true;
                new_refresh_sleep = atoi(optarg);
                if (new_refresh_sleep >= 0) {
                    refresh_sleep = new_refresh_sleep;
                }
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

static bool print_visitor(const char* path, int index, const char* cache) {
    trace("%s %d %s \n", path, index, cache);
    return true;
}

int main(int argc, char* argv[]) {
    process_options(argc, argv);
    dirtab_init();
    if (get_trace() && ! dirtab_is_empty()) {
        trace("loaded dirtab:\n");
        dirtab_visit(print_visitor);
    }
    const char* basedir = dirtab_get_basedir();
    if (chdir(basedir)) {
        report_error("cannot change current directory to %s: %s\n", basedir, strerror(errno));
        exit(FAILED_CHDIR);
    }
    lock_or_unloock(true);
    main_loop();
    if (!dirtab_flush()) {
        report_error("error storing dirtab\n");
    }
    return 0;
}
