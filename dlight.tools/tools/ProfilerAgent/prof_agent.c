#define _GNU_SOURCE
#include <dlfcn.h>
#include <sched.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/times.h>
#include <sys/types.h>
#include <errno.h>
#include <unistd.h>
#include <pthread.h>

#ifndef __APPLE__
#include <malloc.h>
#endif

#include "instruments.h"

#ifdef TRACE
#define LOG(args...) fprintf(stderr, ## args)
#else
#define LOG(...)
#endif

volatile int trace_sync = 0;
volatile int trace_mem = 0;
volatile int trace_cpu = 0;
volatile static int thr_count = 0; /*1; // main thread is always here */

//---- control and reporting section --------------------------

static int msqid = -1;
static void* hndl = NULL;
static struct mallinfo (*mall_hndl)(void) = NULL;

static int reporter(void* arg);
static int send_messages(int cleanup);
static int wait_ack();

static int init_sync_tracing();

static void report_failure(int t) {
    struct failmsg buf = {
        FAILMSG, t
    };
    msgsnd(msqid, &buf, sizeof(buf) - sizeof(buf.type), IPC_NOWAIT);
}

static int pthreads_enabled = 0;
static int mallinfo_enabled = 0;

void cleanup(void* p) {
    if (0 <= msqid) {
        if (send_messages(1)) {
            wait_ack();
        }
        struct msqid_ds msbuf;
        msgctl(msqid, IPC_RMID, &msbuf); // closing the queue
        msqid = -1;
    }
}

static void start_reporting();
static void start_main();
static void setup();

static void
__attribute((constructor))
init_function(void) {
    LOG("init started\n");
    pthreads_enabled = 1; //init_sync_tracing();
    mall_hndl  = dlsym((void*)-1 /*RTLD_NEXT*/, "mallinfo");
    if(!mall_hndl)
        mall_hndl  = dlsym((void*)0 /*RTLD_DEFAULT*/, "mallinfo");
    mallinfo_enabled = (mall_hndl != NULL);

    start_main(); // this sets up everything for main
    start_reporting();
    LOG("init done\n");
}


static void
__attribute((destructor))
fini_function(void) {
    cleanup(0);
}

// ----------------------------------------------------------------
// Each thread has a thread-local lock counter -- thlock.
// This is a longer but more portable way to write: static __thread int thlock

static pthread_key_t thlock_key;
static pthread_once_t thlock_key_once = PTHREAD_ONCE_INIT;

static void make_thlock_key() {
    pthread_key_create(&thlock_key, NULL);
}

void thlock_init() {
    pthread_once(&thlock_key_once, make_thlock_key);
    int* thlock = malloc(sizeof(int));
    pthread_setspecific(thlock_key, (void*) thlock);
}

void thlock_free() {
    pthread_once(&thlock_key_once, make_thlock_key);
    int* thlock = (int*) pthread_getspecific(thlock_key);
    if (thlock) {
        pthread_setspecific(thlock_key, NULL);
        free(thlock);
    }
}

int* thlock_get() {
    pthread_once(&thlock_key_once, make_thlock_key);
    return (int*) pthread_getspecific(thlock_key);
}


//---- tracing section ---------------------------------------------

#if defined(__i386__) || defined(__x86_64)
static long sem = 0;
static void lock() {
    asm volatile (
        "xor %%edx, %%edx\n\t"
        "incl %%edx\n\t"
        "l:\txor %%eax, %%eax\n\t"
        "lock cmpxchg %%edx, %0\n\t"
        "jnz l\n"
        : "=m"(sem)
        : "m"(sem)
        : "edx"
    );
}
static void unlock() {
    asm volatile (
        "xor %%eax, %%eax\n\t"
        "xchg %%eax, %0\n\t"
        : "=m"(sem)
        : "m"(sem)
    );
}
#else
    #error "Non x86 version of lock/unlock is not implemented yet"
#endif

#define ORIG(func) _orig_##func
#define QUOTE(nm) #nm

#define INSTRUMENT(func, param, actual) \
int func param { \
    static int (* ORIG(func)) param = NULL; \
    INIT(func); \
    LOG(QUOTE(func) " called\n"); \
    int* thlock = thlock_get(); \
    if (thlock) { ++(*thlock); } \
    int ret = ORIG(func) actual; \
    if (thlock) { --(*thlock); } \
    return ret; \
}

#define INSTRUMENT2(func, suffix, version, param, actual) \
int func##suffix param { \
    static int (* ORIG(func))param = NULL; \
    INIT2(func, suffix, version); \
    LOG(QUOTE(func) "@" version " called\n"); \
    int* thlock = thlock_get(); \
    if (thlock) { ++(*thlock); } \
    int ret = ORIG(func) actual; \
    if (thlock) { --(*thlock); } \
    return ret; \
}

#define INIT(func) \
    if(!ORIG(func)) { \
        ORIG(func) = dlsym(RTLD_NEXT, QUOTE(func)); \
        if(ORIG(func) && ORIG(func)==func) \
            ORIG(func) = dlsym(RTLD_NEXT, QUOTE(func)); \
        if(!ORIG(func)) \
            ORIG(func) = dlsym(RTLD_DEFAULT, QUOTE(func)); \
    }

#define INIT2(func, suffix, version) \
    if(!ORIG(func)) { \
        ORIG(func) = dlvsym(RTLD_NEXT, QUOTE(func), version); \
        if(ORIG(func) && ORIG(func)==func ## suffix) \
            ORIG(func) = dlvsym(RTLD_NEXT, QUOTE(func), version); \
        if(!ORIG(func)) \
            ORIG(func) = dlvsym(RTLD_DEFAULT, QUOTE(func), version); \
    }

#ifndef __APPLE__

// See IZ 167660 for discussion of the problem and solution
__asm__(".symver pthread_cond_wait_2_0,pthread_cond_wait@GLIBC_2.0");
INSTRUMENT2(pthread_cond_wait, _2_0, "GLIBC_2.0",
        (pthread_cond_t *__restrict __cond, pthread_mutex_t *__restrict __mutex),
        (__cond, __mutex))

__asm__(".symver pthread_cond_timedwait_2_0,pthread_cond_timedwait@GLIBC_2.0");
INSTRUMENT2(pthread_cond_timedwait, _2_0, "GLIBC_2.0",
        (pthread_cond_t *__restrict __cond, pthread_mutex_t *__restrict __mutex, __const struct timespec *__restrict __abstime),
        (__cond, __mutex, __abstime))

__asm__(".symver pthread_cond_wait_2_2_5,pthread_cond_wait@GLIBC_2.2.5");
INSTRUMENT2(pthread_cond_wait, _2_2_5, "GLIBC_2.2.5",
        (pthread_cond_t *__restrict __cond, pthread_mutex_t *__restrict __mutex),
        (__cond, __mutex))

__asm__(".symver pthread_cond_timedwait_2_2_5,pthread_cond_timedwait@GLIBC_2.2.5");
INSTRUMENT2(pthread_cond_timedwait, _2_2_5, "GLIBC_2.2.5",
        (pthread_cond_t *__restrict __cond, pthread_mutex_t *__restrict __mutex, __const struct timespec *__restrict __abstime),
        (__cond, __mutex, __abstime))

__asm__(".symver pthread_cond_wait_2_3_2,pthread_cond_wait@@GLIBC_2.3.2");
INSTRUMENT2(pthread_cond_wait, _2_3_2, "GLIBC_2.3.2",
        (pthread_cond_t *__restrict __cond, pthread_mutex_t *__restrict __mutex),
        (__cond, __mutex))

__asm__(".symver pthread_cond_timedwait_2_3_2,pthread_cond_timedwait@@GLIBC_2.3.2");
INSTRUMENT2(pthread_cond_timedwait, _2_3_2, "GLIBC_2.3.2",
        (pthread_cond_t *__restrict __cond, pthread_mutex_t *__restrict __mutex, __const struct timespec *__restrict __abstime),
        (__cond, __mutex, __abstime))

#else

INSTRUMENT(pthread_cond_wait,
        (pthread_cond_t *__restrict __cond, pthread_mutex_t *__restrict __mutex),
        (__cond, __mutex))

INSTRUMENT(pthread_cond_timedwait,
        (pthread_cond_t *__restrict __cond, pthread_mutex_t *__restrict __mutex, __const struct timespec *__restrict __abstime),
        (__cond, __mutex, __abstime))

#endif

INSTRUMENT(pthread_mutex_lock,
        (pthread_mutex_t *__mutex),
        (__mutex))

INSTRUMENT(pthread_mutex_setprioceiling,
        (pthread_mutex_t *__restrict __mutex, int __prioceiling, int *__restrict __old_ceiling),
        (__mutex, __prioceiling, __old_ceiling))

INSTRUMENT(pthread_rwlock_rdlock,
        (pthread_rwlock_t *__rwlock),
        (__rwlock))

INSTRUMENT(pthread_rwlock_wrlock,
        (pthread_rwlock_t *__rwlock),
        (__rwlock))

#ifndef __APPLE__
// pthread barriers not supported on Apple platform

INSTRUMENT(pthread_barrier_wait,
        (pthread_barrier_t *__barrier),
        (__barrier))

#endif

INSTRUMENT(pthread_mutex_timedlock,
        (pthread_mutex_t *__restrict __mutex, __const struct timespec *__restrict __abstime),
        (__mutex, __abstime))

INSTRUMENT(pthread_rwlock_timedrdlock,
        (pthread_rwlock_t *__restrict __rwlock, __const struct timespec *__restrict __abstime),
        (__rwlock, __abstime))

INSTRUMENT(pthread_rwlock_timedwrlock,
        (pthread_rwlock_t *__restrict __rwlock, __const struct timespec *__restrict __abstime),
        (__rwlock, __abstime))

static int (* ORIG(pthread_create))(void *newthread,
                                    void *attr,
			            void *(*start_routine) (void *),
			            void *arg) = NULL;

static void (* ORIG(pthread_cleanup_push))(void (*routine)(void*), void*);

typedef struct start_pkg {
    void *(*entry_point) (void *);
    void* arg;
} start_pkg;

static int carret = 0;
#define MAXTHR (64)
static int* flags[MAXTHR];
long sync_wait = 0;
struct tms prev_times;
clock_t prev_clock;

int tid_init() {
    int steps = 0;
    while(flags[carret] && steps < (MAXTHR + 1)) {
        carret = (carret + 1) % MAXTHR;
        steps++;
    }
    if (flags[carret] == 0) {
        thlock_init();
        flags[carret] = thlock_get();
        return carret; // found
    } else {
        return -1; // not found
    }
}

void tid_free(int tid) {
    flags[tid] = 0;
    thlock_free();
    carret = tid;
}

void* start_routine(void* pkg) {
    LOG("new thread started\n");
    start_pkg * user_data = (start_pkg *)pkg;
    void *(*user_start_routine) (void *) = user_data->entry_point;
    void *arg = user_data->arg;
    free(user_data);

    lock();
    int tid = tid_init();
    thr_count++;
    unlock();

    user_start_routine(arg);

    lock();
    if (tid) {
        tid_free(tid);
    }
    thr_count--;
    unlock();

    LOG("a thread exited\n");
}

static void start_main() {
    tid_init();
    thr_count++;
}

int pthread_create(pthread_t *__restrict __newthread,
			   __const pthread_attr_t *__restrict __attr,
			   void *(*__start_routine) (void *),
			   void *__restrict __arg)
{
    LOG("pthread_create called\n");
    INIT(pthread_create);
    start_pkg * user_data = malloc(sizeof(start_pkg));
    user_data->entry_point = __start_routine;
    user_data->arg = __arg;
    return ORIG(pthread_create)(__newthread, __attr, start_routine, user_data);
}

static unsigned char reporter_stack[2000];

static void start_reporting() {
    LOG("start_reporting called\n");
#ifdef linux
    clone(reporter, (void*)(reporter_stack+sizeof(reporter_stack)), 
          CLONE_DETACHED | CLONE_THREAD | CLONE_SIGHAND | CLONE_VM, NULL);
#else
    INIT(pthread_create);
    int tid;
    ORIG(pthread_create)(&tid, NULL, reporter, NULL);
#endif
}

static void check_control(int wait) {
    struct ctrlmsg ctrl = {CTRLMSG, 0, 0};
    int r;
    if ((r = msgrcv(msqid, &ctrl, sizeof (ctrl) - sizeof (ctrl.type), CTRLMSG, wait ? 0 : IPC_NOWAIT)) < 0) {
        //perror("ctrl msg fail");
        return;
    }
    LOG("ctrlmsg %d\t %d\n", r, sizeof(ctrl) - sizeof(ctrl.type));
    if ((ctrl.control >> SYNCMSG) & 1) {
        if (pthreads_enabled)
            trace_sync = (ctrl.action >> SYNCMSG) & 1;
        else
            report_failure(SYNCMSG);
    }
    if ((ctrl.control >> MEMMSG) & 1) {
        if (mallinfo_enabled)
            trace_mem = (ctrl.action >> MEMMSG) & 1;
        else
            report_failure(MEMMSG);
    }
    if ((ctrl.control >> SYNCMSG) & 1)
        trace_cpu = (ctrl.action >> CPUMSG) & 1;
    LOG("trace_cpu=%d, trace_mem=%d, trace_sync=%d\n", trace_cpu, trace_mem, trace_sync);
}

static int wait_ack() {
    struct ackrequestmsg request = {ACKREQUEST};
    if (msgsnd(msqid, &request, sizeof(request) - sizeof(request.type), IPC_NOWAIT) >= 0) {
        int i;
        struct ackreplymsg reply = {ACKREPLY};
        for (i = 0; i < 20; ++i) {
            if (msgrcv(msqid, &reply, sizeof(reply) - sizeof(reply.type), ACKREPLY, IPC_NOWAIT) >= 0) {
                return 1;
            }
            usleep(100000);
        }
    }
    return 0;
}

static int send_messages(int cleanup) {
    // on cleanup only memory message is sent
    int sent = 0;
    if (trace_sync && !cleanup) {
        struct syncmsg syncbuf = {
            SYNCMSG,
            (I32) sync_wait,
            (I32) thr_count
        };
        if (msgsnd(msqid, &syncbuf, sizeof(syncbuf) - sizeof(syncbuf.type), IPC_NOWAIT) == 0) {
            ++sent;
        }
    }
    if (trace_cpu && !cleanup) {
        struct tms cur_times;
        clock_t cur_clock = times(&cur_times);
        long delta = (long) (cur_clock - prev_clock);
        struct cpumsg cpubuf = {
            CPUMSG,
            (I32) (100.0 * (cur_times.tms_utime - prev_times.tms_utime) / delta),
            (I32) (100.0 * (cur_times.tms_stime - prev_times.tms_stime) / delta)
        };
        if (msgsnd(msqid, &cpubuf, sizeof(cpubuf) - sizeof(cpubuf.type), IPC_NOWAIT) == 0) {
            ++sent;
        }
        prev_clock = cur_clock;
        prev_times.tms_utime = cur_times.tms_utime;
        prev_times.tms_stime = cur_times.tms_stime;
    }
    if (trace_mem) {
#ifndef __APPLE__
// mallinfo is not supported on Apple platform
        struct mallinfo mi = mall_hndl();
        struct memmsg membuf = {
            MEMMSG,
            mi.uordblks + mi.hblkhd
        };
#else
        struct memmsg membuf = {
            MEMMSG,
            0
        };
#endif
        if (msgsnd(msqid, &membuf, sizeof(membuf) - sizeof(membuf.type), IPC_NOWAIT) == 0) {
            ++sent;
        }
    }
    LOG("sent %d messages\n", sent);
    return sent;
}

int reporter(void* arg) {
    LOG("reporter started\n");
    key_t key = getpid(); // use pid as a name of queue
    int msgflg = IPC_CREAT | 0666;
    if(!(msqid = msgget(key, msgflg)))
 	return -1;
    long resolution = DEF_RES;
    struct timespec w = {0, 0};
#ifdef CLOCK_REALTIME
    if (clock_getres(CLOCK_REALTIME, &w) == 0) {
        resolution = (resolution > w.tv_nsec) ? resolution : w.tv_nsec;
    }
#endif
    w.tv_nsec = (GRANULARITY*resolution);
    long rep_interval = 1000000000L/w.tv_nsec;
    int counter = 0;
    prev_clock = times(&prev_times);
    int i = 0;

    if(ORIG(pthread_cleanup_push))
        ORIG(pthread_cleanup_push)(cleanup, 0);

    check_control(0);

    while (1) {

        nanosleep(&w, NULL);

        //        lock();
        long lock_count = 0;
        for (i = 0; i < MAXTHR; i++) {
            if (flags[i] && *(flags[i]))
                lock_count++;
        }
        //        unlock();
        sync_wait += lock_count;

        if (counter++ > rep_interval) {
            counter = 0;
            send_messages(0);
            check_control(0);
        }
    }
    return 0;
}
