/* 
 * File:   execint.c
 * Author: ll155635
 *
 * Created on July 11, 2010, 3:10 PM
 */

#include <dlfcn.h>
#include <limits.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
//#include <unistd.h>
#include <sys/stat.h>

#ifdef TRACE
#define LOG(args...) fprintf(stderr, ## args)
#else
#define LOG(...)
#endif

/****************************************************************/

static FILE* flog = NULL;
static char* filter[100];
static int filter_sz = 0;

static int comparator(const void * elem1, const void * elem2) {
    const char* str1 = *(const char**) elem1;
    const char* str2 = *(const char**) elem2;
    return strcmp(str1, str2);
}

static int init() {
    /* This function is not reenterable!!! TODO: introduce locking */
    static int interpose_init = 0;
    if (interpose_init != 0)
        return interpose_init;

    char* env_map = getenv("__CND_TOOLS__");
    char* env_log = getenv("__CND_BUILD_LOG__");

    if (env_map == NULL) {
        LOG("\n>>>ERROR: __CND_TOOLS__ is not set!!!\n");
        return interpose_init = -1;
    }
    if (env_log == NULL) {
        LOG("\n>>>ERROR: __CND_BUILD_LOG__ is not set!!!\n");
        return interpose_init = -1;
    }

    flog = fopen(env_log, "a");

    if (flog == NULL) {
        LOG("\n>>>ERROR: can not open%s!!!\n", env_log);
        return interpose_init = -1;
    }

    LOG("\n>>>NBBUILD: TOOLS=%s\n\tLOG=%s\n", env_map, env_log);

    char * token;
    int i = 0;
    for (token = strtok(env_map, ":");
            token;
            token = strtok(NULL, ":")) {
        if (strlen(token) == 0) {
            LOG("\n>>>WARN: TOOLS list contains empty values!!!\n");
            continue;
        }
        char* tail = strrchr(token, '/');
        if (tail) {
            LOG("\n>>>WARN: TOOLS list contains slashes - %s!!!\n", token);
            if (strlen(tail) == 0) {
                LOG("\n>>>WARN: TOOLS list contains directory - %s!!!\n", token);
                continue;
            }
            token = tail;
        }
        filter[i] = strdup(token);
        i++;
        filter_sz++;
    }

    if (filter_sz == 0)
        return interpose_init = -1;

    qsort(filter, filter_sz, sizeof (filter[0]), comparator);

    LOG("\n>>>NBBUILD: INIT DONE\n");

    return interpose_init = 1;
}

static void __logprint(const char* fname, char *const argv[], ...) {

    if (init() < 0) return;

    char* key = strrchr(fname, '/');
    if (key == NULL)
        key = fname;
    else
        key++;

    LOG("\n>>>NBBUILD: key = %s\n", key);

    char** found = bsearch(&key, filter, filter_sz, sizeof (filter[0]), comparator);

    if (found) {
        LOG("\n>>>NBBUILD: found %s\n", *found);
        //FILE* log = stderr;
        fprintf(flog, "called: %s\n", fname);
        char *buf = malloc(1024);
        getcwd(buf, 1024);
        fprintf(flog, "\t%s\n", buf);
        free(buf);
        char** par = (char**) argv;
        for (; *par != 0; par++)
            fprintf(flog, "\t%s\n", *par);
        fprintf(flog, "\n");
        fflush(flog);
    }
    return;
}

#define ORIG(func) _orig_##func
#define QUOTE(nm) #nm

// dirty hack
#define PARG , char** arg
#define PENV  , char** arg, const char** env
#define PVAR  , ...
#define ARG , arg
#define ENV , arg, env

#define INSTRUMENT(func, param, actual) \
int func (const char * p param) { \
    static int (* ORIG(func))(const char* p param) = NULL; \
    INIT(func); \
    LOG(">>>EXECINT: %s called. PATH=%s\n", QUOTE(func), p); \
    __logprint(p actual); \
    int ret = ORIG(func) (p actual); \
    LOG(">>>EXECINT: %s  returned\n", QUOTE(func)); \
    return ret; \
}

#define INIT(func) \
    if(!ORIG(func)) { \
        ORIG(func) = (typeof(ORIG(func)))dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(ORIG(func) && ORIG(func)==func) \
            ORIG(func) = (typeof(ORIG(func)))dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(!ORIG(func)) \
            ORIG(func) = (typeof(ORIG(func)))dlsym((void*)0 /*RTLD_DEFAULT*/, QUOTE(func)); \
    }

#define GETENV

INSTRUMENT(execv, PARG, ARG)
INSTRUMENT(execve, PENV, ENV)
INSTRUMENT(execvp, PARG, ARG)

#define RETURN(f) return f(name, (char **)first)

#define CONVERT(from_func, to_func) \
int from_func(char *name, ...) { \
    va_list args; \
    char**  first; \
    char**  env; \
    va_start(args, name); \
    first = (char**)args; \
    GETENV; \
    va_end(args); \
    LOG(">>>EXECINT: %s converted to %s\n", QUOTE(from_func), QUOTE(to_func)); \
    RETURN(to_func); \
}

CONVERT(execl, execv)
CONVERT(execlp, execvp)

#undef RETURN
#undef GETENV
#define GETENV \
       while (va_arg(args, char *) != (char *)0) {;};\
       env = va_arg(args, char **)
#define RETURN(f) return f(name, (char **)first, (const char**)env)

CONVERT(execle, execve)

static void
__attribute((constructor))
init_function(void) {
}

static void
__attribute((destructor))
fini_function(void) {
    if (flog) {
        fclose(flog);
        LOG("log closed\n");
    }
    int i = 0;
    for (; i < filter_sz; i++)
        free(filter[i]);
}
