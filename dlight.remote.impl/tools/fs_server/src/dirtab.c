
#include "fs_common.h"
#include "dirtab.h"
#include "util.h"

#include "exitcodes.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include <ctype.h>
#include <assert.h>

static char* root = NULL;
static char* temp_path = NULL;
static char* cache_path = NULL;
static char* dirtab_file_path = NULL;
static const char* cahe_subdir_name = "cache";

static pthread_mutex_t cache_mutex = PTHREAD_MUTEX_INITIALIZER;

typedef struct dirtab_element {
    int index;
    char* cache;
    char path[];
} dirtab_element;

/**guarder by dirtab_mutex */
typedef struct dirtab {

    bool dirty;
    
    /** guards all data in the structure */
    pthread_mutex_t mutex;
    
    /** directories count */
    int size;

    /** max amount of paths before realloc */
    int limit;
    
    /** 
     * List of directories under control
     * sorted in alphabetical order
     */
    dirtab_element** paths;
    
    /** the next unoccupied index */
    int next_index;
} dirtab;

static dirtab table;

static void init_table() {
    pthread_mutex_init(&table.mutex, NULL);
    table.dirty =  false;
    table.size =  0;
    table.limit = 1024;
    table.paths = malloc(table.limit * (sizeof(dirtab_element*)));
    table.next_index = 0;
}

static void expand_table_if_needed() {
    if (table.limit <= table.size) {
        table.limit *= 2;
        table.paths = realloc(table.paths, table.limit * (sizeof(dirtab_element*)));
        if (!table.paths) {
            exit(NO_MEMORY_EXPANDING_DIRTAB);
        }
    }    
}

/** to use with qsort */
static int compare_dirtab_elements_4qsort(const void *d1, const void *d2) {
    if (!d1) {
        return d2 ? -1 : 0;
    } else if (!d2) {
        return 1;
    } else {
        dirtab_element *el1 = *((dirtab_element **) d1);
        dirtab_element *el2 = *((dirtab_element **) d2);
        int result = strcmp(el1->path, el2->path);
        return result;
    }
}

/** to use with bsearch */
static int compare_dirtab_elements_4search(const void *d1, const void *d2) {
    if (!d1) {
        return d2 ? -1 : 0;
    } else if (!d2) {
        return 1;
    } else {
        char *path = (char *) d1;
        dirtab_element *el2 = *((dirtab_element **) d2);
        int result = strcmp(path, el2->path);
        return result;
    }
}

static dirtab_element *new_dirtab_element(const char* path, int index) {
    char cache[32];
    sprintf(cache, "%s/%d", cahe_subdir_name, index);
    int path_len = strlen(path);
    int cache_len = strlen(cache);
    int size = sizeof(dirtab_element) + path_len + cache_len + 2;
    dirtab_element *el = malloc(size);
    el->index = index;
    strcpy(el->path, path);
    el->cache = el->path + path_len + 1;
    strcpy(el->cache, cache);
    return el;
}

static const dirtab_element *add_path(const char* path) {
    dirtab_element *el = new_dirtab_element(path, table.next_index++);
    strcpy(el->path, path);
    expand_table_if_needed();
    table.paths[table.size++] = el;
    table.dirty = true;
    qsort(table.paths, table.size, sizeof(dirtab_element *), compare_dirtab_elements_4qsort);
    return el;
}

static bool load_impl() {
    FILE *f = fopen(dirtab_file_path, "r");
    if (!f) {
        report_error("error opening %s: %s\n", dirtab_file_path, strerror(errno));
        return false;
    }
    int max_line = PATH_MAX + 40;
    char *line = malloc(max_line);
    table.size = 0;
    table.next_index = 0;
    while (fgets(line, max_line, f)) {
        int index = 0;
        char* p = line;
        while (isdigit(*p)) {
            index *= 10;
            index += (*p) - '0';
            p++;
        }
        if (*p != ' ') {
            report_error("error in file %s: index not followed by space in line '%s'\n", dirtab_file_path, line);
            return false; //TODO: clear the table!
        }
        char* path = ++p;
        // cut off '\n\ before trailing '\0'
        while (*p++);
        // p points to trailing '\0'
        if (p >= path) {
            p--;
        }
        if (p >= path) {
            p--;
        }
        if (*p == '\n') {
            *p = 0;
        }
        unescape_strcpy(path, path);
        expand_table_if_needed();
        table.paths[table.size] = new_dirtab_element(path, index);
        table.size++;
        if (index + 1 > table.next_index) {
            table.next_index = index + 1;
        }
    }
    free(line);
    if (fclose(f) == 0) {
        return true;
    } else {
        report_error("error closing %s: %s\n", dirtab_file_path, strerror(errno));
        return false;
    }
}

static bool load_table() {
    if (!file_exists(dirtab_file_path)) {
        return false;
    }
    mutex_lock(&table.mutex);
    bool result = load_impl();
    mutex_unlock(&table.mutex);
    return result;    
}

static bool flush_impl() {
    FILE *fp = fopen600(dirtab_file_path);
    if (!fp){
        report_error("error opening %s for writing: %s\n", dirtab_file_path, strerror(errno));
        return false;
    }
    int i;
    char* buf = malloc(PATH_MAX * 2); 
    for (i = 0; i < table.size; i++) {
        escape_strcpy(buf, table.paths[i]->path);
        fprintf(fp, "%d %s\n", table.paths[i]->index, buf);
    }
    free(buf);
    if (fclose(fp) == 0) {
        return true;
    } else {
        report_error("error closing %s for writing: %s\n", dirtab_file_path, strerror(errno));
        return false;
    }
}

bool dirtab_flush() {
    mutex_lock(&table.mutex);
    bool result;
    if (table.dirty) {
        result = flush_impl();
        table.dirty = false;
    } else {
        result = true;
    }
    mutex_unlock(&table.mutex);
    return result;
}

const char* dirtab_get_tempdir() {
    return temp_path;
}

const char* dirtab_get_basedir() {
    return root;
}

static void mkdir_or_die(const char *path, int exit_code_fail_create, int exit_code_fail_access) {
    struct stat stat_buf;
    if (lstat(path, &stat_buf) == -1) {
        if (errno == ENOENT) {
            if (mkdir(path, 0700) != 0) {
                report_error("error creating directory '%s': %s\n", path, strerror(errno));
                exit(exit_code_fail_create);
            }
        } else {
            report_error("error accessing directory '%s': %s\n", path, strerror(errno));
            exit(exit_code_fail_access);
        }
    }    
}

void dirtab_init() {

    root = malloc(PATH_MAX);
    temp_path = malloc(PATH_MAX);
    cache_path = malloc(PATH_MAX);
    dirtab_file_path = malloc(PATH_MAX);

    const char* home = get_home_dir();
    if (!home) {
        exit(FAILURE_GETTING_HOME_DIR);
    }
    strncpy(root, home, PATH_MAX);

    strcat(root, "/.netbeans");    
    mkdir_or_die(root, FAILURE_CREATING_STORAGE_SUPER_DIR, FAILURE_ACCESSING_STORAGE_SUPER_DIR);
        
    strcat(root, "/remotefs");
    mkdir_or_die(root, FAILURE_CREATING_STORAGE_DIR, FAILURE_ACCESSING_STORAGE_DIR);
    
    strcpy(cache_path, root);
    strcat(cache_path, "/");
    strcat(cache_path, cahe_subdir_name);
    mkdir_or_die(cache_path, FAILURE_CREATING_CACHE_DIR, FAILURE_ACCESSING_CACHE_DIR);

    strcpy(temp_path, root);
    strcat(temp_path, "/tmp");
    mkdir_or_die(temp_path, FAILURE_CREATING_TEMP_DIR, FAILURE_ACCESSING_TEMP_DIR);

    strcpy(dirtab_file_path, root);
    strcat(dirtab_file_path, "/dirtab");
    
    init_table();
    load_table();
}

void dirtab_free() {
    mutex_lock(&table.mutex);
    for (int i = 0; i < table.size; i++) {
        free(table.paths[i]);
    }
    table.size = 0;
    free(table.paths);
    mutex_unlock(&table.mutex);
    free(root);
    free(temp_path);
    free(cache_path);
    free(dirtab_file_path);    
    // just in case:
    root = NULL;
    temp_path = NULL;
    cache_path = NULL;
    dirtab_file_path = NULL;
}

void  dirtab_lock_cache_mutex() {
    mutex_lock(&cache_mutex);
}

void  dirtab_unlock_cache_mutex() {
    mutex_unlock(&cache_mutex);
}

const char* dirtab_get_cache(const char* path) {

    mutex_lock(&table.mutex);
    
    const dirtab_element *el;
    
    dirtab_element **found = (dirtab_element**) bsearch(path, table.paths, table.size, 
            sizeof(dirtab_element *), compare_dirtab_elements_4search);
    if (found) {
        el = *found;
    } else {
        el = add_path(path);
    }

    mutex_unlock(&table.mutex);

    return el->cache;
}

void dirtab_visit(bool (*visitor) (const char* path, int index, const char* cache)) {
    mutex_lock(&table.mutex);
    int size = table.size;
    int mem_size = size * sizeof(dirtab_element**);
    dirtab_element** paths = malloc(mem_size);
    memcpy(paths, table.paths, mem_size);
    mutex_unlock(&table.mutex);
    for (int i = 0; i < size; i++) {
        dirtab_element* el = paths[i];
        bool proceed = visitor(el->path, el->index, el->cache);
        if (!proceed) {
            break;
        }
    }
    free(paths);
}

bool dirtab_is_empty() {
    mutex_lock(&table.mutex);
    int size = table.size;
    mutex_unlock(&table.mutex);
    return size == 0;
}
