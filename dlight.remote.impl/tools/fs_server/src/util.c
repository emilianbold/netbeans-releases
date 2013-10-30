#include "fs_common.h"
#include "util.h"
#include "exitcodes.h"

#include <stdio.h> 
#include <stdarg.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <pwd.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/time.h>

static bool trace_flag = false;
static FILE *log_file = NULL;

void set_trace(bool on_off) {
    trace_flag= on_off;
}

bool get_trace() {
    return trace_flag;
}

void report_error(const char *format, ...) {
    va_list args;
    va_start (args, format);
    fprintf(stderr, "fs_server[%li]: ", (long) getpid());
    vfprintf(stderr, format, args);
    va_end (args);
}

void trace(const char *format, ...) {
    if (trace_flag) {
        va_list args;
        va_start(args, format);
        fprintf(stderr, "fs_server[%li]: ", (long) getpid());
        vfprintf(stderr, format, args);
        va_end(args);  
        fflush(stderr);
    }
}

void log_print(const char *format, ...) {
    if (log_file) {
        va_list args;
        va_start(args, format);
        vfprintf(log_file, format, args);
        va_end(args);  
        fflush(log_file);
    }
}

void log_open(const char* path) {
    int fd = open(path, O_WRONLY | O_APPEND | O_CREAT, 0600);
    if (fd == -1) {
        log_file = NULL;
    } else {
        log_file = fdopen(fd, "a");
    }
}

void log_close() {    
    if (log_file) {
        fclose(log_file);
    }
}

void soft_assert(int condition, char* format, ...) {
    if (! condition) {
        va_list args;
        va_start(args, format);
        vfprintf(stderr, format, args);
        va_end(args);
    }
}

void mutex_lock(pthread_mutex_t *mutex) {
    if (pthread_mutex_lock(mutex)) {
        report_error("error unlocking mutex: %s\n", strerror(errno));
        exit(FAILURE_LOCKING_MUTEX);
    }
}

void mutex_unlock(pthread_mutex_t *mutex) {
    if (pthread_mutex_unlock(mutex)) {
        report_error("error unlocking mutex: %s\n", strerror(errno));
        exit(FAILURE_UNLOCKING_MUTEX);
    }
}

const char* get_home_dir() {
    uid_t uid = getuid();
    struct passwd *pw = getpwuid(uid);
    return pw ? pw->pw_dir: NULL;
}

bool file_exists(const char* path) {
    struct stat stat_buf;
    if (lstat(path, &stat_buf) == -1 ) {
        return errno != ENOENT;        
    }
    return true;
}

int fclose_if_not_null(FILE* f) {
    return f ? fclose(f) : 0;
}

int closedir_if_not_null(DIR *d) {
    return d ? closedir(d) : 0;
}


static int __thread long stopwatch_start_time;

void stopwatch_start() {
    if (trace_flag) {
        struct timeval curr_time;
        gettimeofday(&curr_time, 0);
        stopwatch_start_time = curr_time.tv_sec * 1000 + curr_time.tv_usec;
    }
}

void stopwatch_stop(const char* message) {
    if (trace_flag) {
        struct timeval curr_time;
        gettimeofday(&curr_time, 0);
        long end_time = curr_time.tv_sec * 1000 + curr_time.tv_usec;
        trace("%s took %d ms\n", message, end_time - stopwatch_start_time);
    }    
}

char *replace_first(char *s, char c, char replacement) {
    if (s) {
        char* p = strchr(s, c);
        if (p) {
            *(p) = replacement;
        }
    }
    return s;
}

FILE* fopen600(const char* path) {
    int fd = open(path, O_WRONLY | O_CREAT, 0600);
    if (fd == -1) {
        return NULL;
    } else {
        return fdopen(fd, "w");
    }
}
