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
#include <signal.h>

static TraceLevel trace_level = TRACE_NONE;
static FILE *log_file = NULL;

void set_trace(TraceLevel new_level) {
    trace_level= new_level;
}

bool is_traceable(TraceLevel level) {
    return (trace_level >= level);
}

void report_error(const char *format, ...) {
    va_list args;
    va_start (args, format);
    fprintf(stderr, "fs_server[%li]: ", (long) getpid());
    vfprintf(stderr, format, args);
    va_end (args);
}

void trace(TraceLevel level, const char *format, ...) {
    if (trace_level >= level) {
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
        report_error("error locking mutex: %s\n", strerror(errno));
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

bool dir_exists(const char* path) {
    struct stat stat_buf;
    if (stat(path, &stat_buf) == -1 ) {
        return errno != ENOENT;        
    }
    return S_ISDIR(stat_buf.st_mode);
}

int fclose_if_not_null(FILE* f) {
    return f ? fclose(f) : 0;
}

int closedir_if_not_null(DIR *d) {
    return d ? closedir(d) : 0;
}


static int __thread long stopwatch_start_time;

void stopwatch_start() {
    if (trace_level) {
        struct timeval curr_time;
        gettimeofday(&curr_time, 0);
        stopwatch_start_time = curr_time.tv_sec * 1000 + curr_time.tv_usec / 1000;
    }
}

void stopwatch_stop(TraceLevel level, const char* message) {
    if (trace_level >= level) {
        struct timeval curr_time;
        gettimeofday(&curr_time, 0);
        long end_time = curr_time.tv_sec * 1000 + curr_time.tv_usec / 1000;
        trace(level, "%s took %d ms\n", message, end_time - stopwatch_start_time);
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
    int fd = open(path, O_WRONLY | O_TRUNC | O_CREAT, 0600);
    if (fd == -1) {
        return NULL;
    } else {
        return fdopen(fd, "w");
    }
}

/**
 * escapre rules are:
 * "\n" -> "\\n"
 * "\" -> "\\\\"
 */
int escape_strlen(const char* s) {
    if (!s) {
        return 0;
    }
    int len = 0;  
    for (const char *p = s; *p; p++) {
        len += (*p == '\n' || *p == '\\') ? 2 : 1;
    }
    return len;
}

char *escape_strcpy(char *dst, const char *src) {
    char* d = dst;
    for (const char *p = src; *p; p++) {
        if (*p == '\n') {
            *d++ = '\\';
            *d++ = 'n';
        } else if (*p == '\\') {
            *d++ = '\\';
            *d++ = '\\';
        } else {
            *d++ = *p;
        }
    }
    *d = 0;
    return dst;
}

int unescape_strlen(const char* s) {    
    bool escape = false;
    int len = 0;
    for (const char *p = s; *p; p++) {
        if (escape) {
            escape = false;
            len++;
        } else {
            if (*p == '\\') {
                escape = true;
            } else {
                len++;
            }
        }
    }
    return len;
}

char *unescape_strcpy(char *dst, const char *src) {
    bool escape = false;
    char *d = dst;
    for (const char *p = src; *p; p++) {
        if (escape) {
            escape = false;
            if (*p == '\\') {
                *d++ = '\\';
            } else if (*p == 'n') {
                *d++ = '\n';
            } else {
                report_error("wrong character '%c' in line %s\n", *p, src);
                *d++ = *p;
            }
        } else {
            if (*p == '\\') {
                escape = true;
            } else {
                *d++ = *p;
            }
        }
    }
    *d = 0;
    return dst;
}

char* signal_name(int signal) {
    switch (signal) {
        case SIGHUP:    return "SIGHUP";
        case SIGINT:    return "SIGINT";
        case SIGQUIT:   return "SIGQUIT";
        case SIGILL:    return "SIGILL";
        case SIGTRAP:   return "SIGTRAP";
        case SIGABRT:   return "SIGABRT";
//        case SIGIOT:    return "SIGIOT";
        case SIGBUS:    return "SIGBUS";
        case SIGFPE:    return "SIGFPE";
        case SIGKILL:   return "SIGKILL";
        case SIGUSR1:   return "SIGUSR1";
        case SIGSEGV:   return "SIGSEGV";
        case SIGUSR2:   return "SIGUSR2";
        case SIGPIPE:   return "SIGPIPE";
        case SIGALRM:   return "SIGALRM";
        case SIGTERM:   return "SIGTERM";
#if __linux__        
        case SIGSTKFLT: return "SIGSTKFLT";
#endif        
//        case SIGCLD:    return "SIGCLD"; // dup
        case SIGCHLD:   return "SIGCHLD";
        case SIGCONT:   return "SIGCONT";
        case SIGSTOP:   return "SIGSTOP";
        case SIGTSTP:   return "SIGTSTP";
        case SIGTTIN:   return "SIGTTIN";
        case SIGTTOU:   return "SIGTTOU";
        case SIGURG:    return "SIGURG";
        case SIGXCPU:   return "SIGXCPU";
        case SIGXFSZ:   return "SIGXFSZ";
        case SIGVTALRM: return "SIGVTALRM";
        case SIGPROF:   return "SIGPROF";
        case SIGWINCH:  return "SIGWINCH";
//        case SIGPOLL:   return "SIGPOLL"; // dup
        case SIGIO:     return "SIGIO (SIGPOLL)";
#if __linux__ || __sun__        
        case SIGPWR:    return "SIGPWR";
#endif        
#if __FreeBSD__
        case SIGINFO:    return "SIGINFO";
#endif        
        default:        return "SIG???";
    }
}
