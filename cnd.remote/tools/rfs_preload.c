#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <memory.h>
#include <errno.h>
#include <unistd.h>
#include <sys/stat.h>
#include  <stdarg.h>
#include  <string.h>
#include  <limits.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <fcntl.h>

#include "rfs_controller.h"
#include "rfs_util.h"

/** the name of the directory under control, including trailing "\" */
static const char *my_dir = 0;
static int my_dir_len;

//static char* curr_dir = 0;
//static int curr_dir_len = 0;

#define get_real_addr(name) _get_real_addr(#name, name);

static inline void *_get_real_addr(const char *name, void* wrapper_addr) {
    void *res;
    int saved_errno = errno;
    res = dlsym(RTLD_NEXT, name);
    if (res && res == wrapper_addr) {
        res = dlsym(RTLD_NEXT, name);
    }
    if (!res) {
        res = dlsym(RTLD_DEFAULT, name);
    }
    errno = saved_errno;
    return res;
}

#if TRACE
static void dbg_print_addr(const char* name) {
    void* addr = dlsym(RTLD_NEXT, name);
    trace("\t%s=%X\n", name, addr);
}
#endif

#if TRACE
static inline void print_dlsym() {
    const char* names[] = {
        "lstat", "_lstat", "stat", "_lxstat", "fstat", "lstat64", "fstat64", "_fxstat",
        "open", "fopen", "open64", "pthread_self",
        "readdir", "access", "utime", 0
    };
    int i = 0;
    while (names[i]) {
        dbg_print_addr(names[i]);
        i++;
    }
}
#endif

static int open_socket() {
    int port = default_controller_port;
    char *env_port = getenv("RFS_CONTROLLER_PORT");
    if (env_port) {
        port = atoi(env_port);
    }
    char* hostname = "localhost";
    char *env_host = getenv("RFS_CONTROLLER_HOST");
    if (env_host) {
        hostname = env_host;
    }
    trace("Connecting %s:%d\n", hostname, port);
    struct hostent *hp;
    if ((hp = gethostbyname(hostname)) == 0) {
        perror("gethostbyname");
        return -1;
    }
    struct sockaddr_in pin;
    memset(&pin, 0, sizeof (pin));
    pin.sin_family = AF_INET;
    pin.sin_addr.s_addr = ((struct in_addr *) (hp->h_addr))->s_addr;
    pin.sin_port = htons(port);
    int sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd == -1) {
        perror("socket");
        return -1;
    }
    if (connect(sd, (struct sockaddr *) & pin, sizeof (pin)) == -1) {
        perror("connect");
        return -1;
    }
    return sd;
}

static int get_socket_descriptor(int create) {
    // 0 means unitialized
    // -1 means that we failed to open a socket
    static __thread int sd = 0; // socket descriptor
    if (!create) {
        return sd;
    }
    if (sd == -1) {
        return -1;
    }
    if (sd == 0) {
        sd = -1; // in the case of success, it will become > 0
    }
    sd = open_socket();
    return sd;
}

/* static int is_mine(const char *path) {
    if (path[0] == '/') {
        return (strncmp(my_dir, path, my_dir_len) == 0);
    } else {
        // TODO: make an honest comparison
        if (strncmp(my_dir, curr_dir, my_dir_len) == 0) { // with trailing "/"
            return true;
        }
        if (strncmp(my_dir, curr_dir, my_dir_len-1) == 0) { // w/o trailing "/"
            return true;
        }
        return false;
    }
}*/

/** 
 * Does the same as realpath, except for it
 * - does not resolve symlinks
 * - does not call getcwd each time
static char * my_realpath(const char *file_name, char *resolved_name, int resolved_name_size) {
    if (file_name == NULL || resolved_name == NULL || file_name[0] == 0 || resolved_name_size < 2) {
        return NULL;
    } else if (file_name[0] == '/') {
        strncpy(resolved_name, file_name, resolved_name_size);
        return resolved_name;
    }
    // TODO: write a honest implementation!
    if (file_name[0] == '.' && file_name[0] == '/') {
        file_name += 2;
    }
    ...

    return resolved_name;
}*/

/**
 * Called upon opening a file; returns "boolean" success
 * @return true means "ok" (either file is in already sync,
 * or it has been just synched, or or the path isn't under control;
 * false means that the file is ourm, but can't be synched
 */
static int on_open(const char *path, int flags) {
    static int __thread inside = 0;
    if (inside) {
        trace("%s recursive - returning\n", path);
        return true; // recursive!
    }
    if (flags & (O_TRUNC |  O_WRONLY | O_RDWR | O_CREAT)) { // don't need existent content
        trace("%s O_TRUNC |  O_WRONLY | O_RDWR | O_CREAT - returning\n", path);
        return true;
    }
    if (my_dir == 0) { // isn't yet initialized?
        trace("%s not yet initialized - returning\n", path);
        return true;
    }
    inside = 1;

    if (path[0] != '/') {
        static __thread char real_path[PATH_MAX];
        if ( realpath(path, real_path)) {
            path = real_path;
        } else {
            trace("Can not resolve path %s\n", path);
            inside = 0;
            return false;
        }
    }

    if (strncmp(my_dir, path, my_dir_len) != 0) {
        trace("%s is not mine\n", path);
        inside = 0;
        return true;
    }
    int result = false;
    int sd = get_socket_descriptor(1);
    if (sd == -1) {
        trace("On open %s: sd == -1\n", path);
    } else {
        //struct rfs_request;
        int path_len = strlen(path);
        trace("Sending %s (%d bytes)\n", path, path_len);
        if (send(sd, path, path_len, 0) == -1) {
            perror("send");
        } else {
            char response_buf[512];
            memset(response_buf, 0, sizeof(response_buf));
            int response_size = recv(sd, response_buf, sizeof (response_buf), 0);
            if (response_size == -1) {
                perror("receive");
                // TODO: correct error processing
            } else {
                trace("Got %s for %s, %d\n", response_buf, path, flags);
                if (response_buf[0] == response_ok) {
                    result = true;
                } else if (response_buf[0] == response_failure) {
                    result = false;
                } else {
                    trace("Protocol error\n");
                    result = false;
                }
            }
        }
    }
    inside = 0;
    return result;
}

void
__attribute__((constructor))
on_startup(void) {
//#if TRACE
//    print_dlsym();
//#endif
    //curr_dir = malloc(curr_dir_len = PATH_MAX);
    //getcwd(curr_dir, curr_dir_len);
    my_dir = getenv("RFS_CONTROLLER_DIR");
    if (!my_dir) {
        //my_dir = curr_dir;
        char* p = malloc(PATH_MAX);
        getcwd(p, PATH_MAX);
        my_dir = p;
    }
    my_dir_len = strlen(my_dir);
    if (my_dir[my_dir_len-1] == '/') {
        my_dir = strdup(my_dir);
    } else {
        my_dir_len++;
        void *p = malloc(my_dir_len + 1);
        strcat(p, my_dir);
        strcat(p, "/");
        my_dir = p;
    }
    trace("RFS startup; my dir: %s\n", my_dir);
}

void
__attribute__((destructor))
on_shutdown(void) {
    trace("RFS shutdown\n");
    int sd = get_socket_descriptor(0);
    if (sd != -1) {
        close(sd);
    }
}

typedef struct pthread_routine_data {
    void *(*user_start_routine) (void *);
    void* arg;
} pthread_routine_data;

static void* pthread_routine_wrapper(void* data) {
    pthread_routine_data *prd = (pthread_routine_data*) data;
    trace("Starting user thread routine.\n");
    prd->user_start_routine(prd->arg);
    trace("User thread routine finished. Performing cleanup\n");
    free(data);
    int sd = get_socket_descriptor(0);
    if (sd != -1) {
        trace("Closing socked %d\n", sd);
        close(sd);
    }
    return 0;
}

int pthread_create(void *newthread,
        void *attr,
        void *(*user_start_routine) (void *),
        void *arg) {
    trace("pthread_create\n");
    static int (*prev)(void *, void*, void * (*)(void *), void*);
    if (!prev) {
        prev = (int (*)(void*, void*, void * (*)(void *), void*)) get_real_addr(pthread_create);
    }
    pthread_routine_data *data = malloc(sizeof (pthread_routine_data));
    // TODO: check for null???
    data->user_start_routine = user_start_routine;
    data->arg = arg;
    prev(newthread, attr, pthread_routine_wrapper, data);
}

/* int chdir(const char *dir) {
    trace("chdir %s\n", dir);
    static int (*prev) (const char*);
    if (!prev) {
        prev = (int (*) (const char*)) get_real_addr(chdir);
    }
    int res = prev(dir);
    if (res == 0) {
        int len = strlen(dir);
        if (len >= curr_dir_len) {
            free(curr_dir);
            curr_dir = malloc(curr_dir_len = len * 2);
        }
        strcpy(curr_dir, dir);
    }
    return res;
}*/

int open(const char *path, int flags, ...) {
    trace("open %s %d\n", path, flags);

    va_list ap;
    mode_t mode;

    va_start(ap, flags);
    mode = va_arg(ap, mode_t);
    va_end(ap);

    static int (*prev)(const char *, int, mode_t);
    if (!prev) {
        prev = (int (*)(const char *, int, mode_t)) get_real_addr(open);
    }
    if (on_open(path, flags)) {
        return prev(path, flags, mode);
    } else {
        return -1;
    }
}

int open64(const char *path, int flags, ...) {
    trace("open64 %s %d\n", path, flags);

    va_list ap;
    mode_t mode;

    va_start(ap, flags);
    mode = va_arg(ap, mode_t);
    va_end(ap);

    static int (*prev)(const char *, int, mode_t);
    if (!prev) {
        prev = (int (*)(const char *, int, mode_t)) get_real_addr(open64);
    }
    if (on_open(path, flags)) {
        return prev(path, flags, mode);
    } else {
        return -1;
    }
}

int _open(const char *path, int flags, ...) {
    trace("_open %s %d\n", path, flags);

    va_list ap;
    mode_t mode;

    va_start(ap, flags);
    mode = va_arg(ap, mode_t);
    va_end(ap);

    static int (*prev)(const char *, int, mode_t);
    if (!prev) {
        prev = (int (*)(const char *, int, mode_t)) get_real_addr(_open);
    }
    if (on_open(path, flags)) {
        return prev(path, flags, mode);
    } else {
        return -1;
    }
}

int _open64(const char *path, int flags, ...) {
    trace("_open64 %s %d\n", path, flags);

    va_list ap;
    mode_t mode;

    va_start(ap, flags);
    mode = va_arg(ap, mode_t);
    va_end(ap);

    static int (*prev)(const char *, int, mode_t);
    if (!prev) {
        prev = (int (*)(const char *, int, mode_t)) get_real_addr(_open64);
    }
    if (on_open(path, flags)) {
        return prev(path, flags, mode);
    } else {
        return -1;
    }
}

/* int lstat64(const char *_RESTRICT_KYWD path, struct stat64 *_RESTRICT_KYWD buf)
{
    trace("lstat64 %s\n", path);
    static int (*prev)(const char *_RESTRICT_KYWD, struct stat64 *_RESTRICT_KYWD);
    if (!prev) {
        prev = (int (*)(const char *_RESTRICT_KYWD, struct stat64 *_RESTRICT_KYWD)) get_real_addr("lstat64");
    }
    return prev(path, buf);
}

int _lxstat(const int mode, const char *path, struct stat *buf) {
    trace("_lxstat %d %s\n", mode, path);
    static int (*prev)(const int, const char *, struct stat*);
    if (!prev) {
        prev = (int (*)(const int, const char *, struct stat*)) get_real_addr("_lxstat");
    }
    return prev(mode, path, buf);
} */
