#define _GNU_SOURCE
#include <dlfcn.h>
#include <malloc.h>
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
        struct msqid_ds msbuf;
        msgctl(msqid, IPC_RMID, &msbuf); // closing the queue
        msqid = -1;
    }
}

static void start_reporting();
static void start_main();
static void setup();

//int main(int argc, void** argv);

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
    #error "Non x86 version of lock/unlock is not inplemented yet"
#endif

#define ORIG(func) _orig_##func
#define QUOTE(nm) #nm
// dirty hack
#define VOID1P , void* p1
#define VOID2P , void* p1, void* p2
#define INT2P  , int p1, int* p2
#define ACTUAL1 , p1
#define ACTUAL2 , p1, p2

#define INSTRUMENT(func, param, actual) \
int func (void * p param) { \
    static int (* ORIG(func))(void* p param) = NULL; \
    INIT(func); \
    LOG(QUOTE(func) " called\n"); \
    thlock++; \
    int ret = ORIG(func) (p actual); \
    thlock--; \
    return ret; \
}

#define INSTRUMENT2(func, suffix, version, param, actual) \
int func##suffix (void * p param) { \
    static int (* ORIG(func))(void* p param) = NULL; \
    INIT2(func, suffix, version); \
    LOG(QUOTE(func) "@" version " called\n"); \
    thlock++; \
    int ret = ORIG(func) (p actual); \
    thlock--; \
    return ret; \
}

static __thread int thlock = 0;

#define INIT(func) \
    if(!ORIG(func)) { \
        ORIG(func) = dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(ORIG(func) && ORIG(func)==func) \
            ORIG(func) = dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(!ORIG(func)) \
            ORIG(func) = dlsym((void*)0 /*RTLD_DEFAULT*/, QUOTE(func)); \
    }

#define INIT2(func, suffix, version) \
    if(!ORIG(func)) { \
        ORIG(func) = dlvsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func), version); \
        if(ORIG(func) && ORIG(func)==func ## suffix) \
            ORIG(func) = dlvsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func), version); \
        if(!ORIG(func)) \
            ORIG(func) = dlvsym((void*)0 /*RTLD_DEFAULT*/, QUOTE(func), version); \
    }

// See IZ 167660 for discussion of the problem and solution
__asm__(".symver pthread_cond_wait_2_0,pthread_cond_wait@GLIBC_2.0");
INSTRUMENT2(pthread_cond_wait, _2_0, "GLIBC_2.0", VOID1P, ACTUAL1)

__asm__(".symver pthread_cond_timedwait_2_0,pthread_cond_timedwait@GLIBC_2.0");
INSTRUMENT2(pthread_cond_timedwait, _2_0, "GLIBC_2.0", VOID2P, ACTUAL2)

__asm__(".symver pthread_cond_wait_2_2_5,pthread_cond_wait@GLIBC_2.2.5");
INSTRUMENT2(pthread_cond_wait, _2_2_5, "GLIBC_2.2.5", VOID1P, ACTUAL1)

__asm__(".symver pthread_cond_timedwait_2_2_5,pthread_cond_timedwait@GLIBC_2.2.5");
INSTRUMENT2(pthread_cond_timedwait, _2_2_5, "GLIBC_2.2.5", VOID2P, ACTUAL2)

__asm__(".symver pthread_cond_wait_2_3_2,pthread_cond_wait@@GLIBC_2.3.2");
INSTRUMENT2(pthread_cond_wait, _2_3_2, "GLIBC_2.3.2", VOID1P, ACTUAL1)

__asm__(".symver pthread_cond_timedwait_2_3_2,pthread_cond_timedwait@@GLIBC_2.3.2");
INSTRUMENT2(pthread_cond_timedwait, _2_3_2, "GLIBC_2.3.2", VOID2P, ACTUAL2)

//INSTRUMENT(pthread_cond_wait, VOID1P, ACTUAL1)
//INSTRUMENT(pthread_cond_timedwait, VOID2P, ACTUAL2)
INSTRUMENT(pthread_mutex_lock, , )
INSTRUMENT(pthread_mutex_setprioceiling, INT2P, ACTUAL2)
INSTRUMENT(pthread_rwlock_rdlock, , )
INSTRUMENT(pthread_rwlock_wrlock, , )
INSTRUMENT(pthread_barrier_wait, , )
//INSTRUMENT(pthread_join, VOID1P, ACTUAL1)
INSTRUMENT(pthread_mutex_timedlock, VOID1P, ACTUAL1)
INSTRUMENT(pthread_rwlock_timedrdlock, VOID1P, ACTUAL1)
INSTRUMENT(pthread_rwlock_timedwrlock, VOID1P, ACTUAL1)
INSTRUMENT(pthread_spin_lock, , )
//INSTRUMENT(pthread_cleanup_push);

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


void* start_routine(void* pkg) {
    LOG("new thread started\n");
    int tid = 0;
    start_pkg * user_data = (start_pkg *)pkg;
    void *(*user_start_routine) (void *) = user_data->entry_point;
    void *arg = user_data->arg;
    free(user_data);
    thlock = 0;
    lock();
    int steps = 0;
    while(flags[carret] && steps < (MAXTHR + 1)) {
        carret = (carret + 1) % MAXTHR;
        steps++;
    }
    int found = (flags[carret] == 0);
    if(found) {
        tid = carret;
        flags[tid] = &thlock;
        thlock = 0;
        carret = (carret + 1) % MAXTHR;
    }
    thr_count++;
    unlock();
    user_start_routine(arg);
    lock();
    if(found) {
        flags[tid] = NULL;
        carret = tid;
    }
    thr_count--;
    unlock();
    LOG("a thread exited\n");
}

static void start_main() {
    int tid = 0;
    thlock = 0;
 //   lock();
    int steps = 0;
    while(flags[carret] && steps < (MAXTHR + 1)) {
        carret = (carret + 1) % MAXTHR;
        steps++;
    }
    int found = (flags[carret] == 0);
    if(found) {
        tid = carret;
        flags[tid] = &thlock;
        thlock = 0;
        carret = (carret + 1) % MAXTHR;
    }
    thr_count++;
   // unlock();
}

int pthread_create(void *newthread,
                   void *attr,
		   void *(*user_start_routine) (void *),
		   void *arg)
{
    LOG("pthread_create called\n");
    INIT(pthread_create);
    start_pkg * user_data = malloc(sizeof(start_pkg));
    user_data->entry_point = user_start_routine;
    user_data->arg = arg;
    return ORIG(pthread_create)(newthread, attr, start_routine, user_data);
}
/*
int init_sync_tracing() {
    if ((hndl = dlopen("libpthread.so.0", RTLD_LAZY))) {
        INIT(pthread_cond_wait);
        INIT(pthread_cond_timedwait);
        INIT(pthread_mutex_lock);
        INIT(pthread_mutex_setprioceiling);
        INIT(pthread_rwlock_rdlock);
        INIT(pthread_rwlock_wrlock);
        INIT(pthread_barrier_wait);
        INIT(pthread_join);
        INIT(pthread_mutex_timedlock);
        INIT(pthread_rwlock_timedrdlock);
        INIT(pthread_rwlock_timedwrlock);
        INIT(pthread_spin_lock);
        INIT(pthread_create);
        INIT(pthread_cleanup_push);
    }

    return (_orig_pthread_mutex_lock != NULL);
}
*/
static unsigned char reporter_stack[2000];

static void start_reporting() {
    LOG("start_reporting called\n");
#ifdef linux
    clone(reporter, (void*)(reporter_stack+sizeof(reporter_stack)), 
          CLONE_DETACHED | CLONE_THREAD | CLONE_SIGHAND | CLONE_VM, NULL);
#else
    int tid;
    pthread_create(&tid, NULL, reporter, NULL);
#endif
}

static void check_control(int wait) {
    struct ctrlmsg ctrl = {CTRLMSG, 0, 0};
    int r;
    if ((r = msgrcv(msqid, &ctrl, sizeof (ctrl) - sizeof (ctrl.type), CTRLMSG, wait ? 0 : IPC_NOWAIT)) < 0) {
        //perror("ctrl msg fail");
        return;
    }
    //printf("ctrlmsg %d\t %d\n", r, sizeof (ctrl) - sizeof (ctrl.type));
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
    //printf("%d, %d, %d\n", trace_cpu, trace_mem, trace_sync);
}

int reporter(void* arg) {
    LOG("reporter started\n");
    key_t key = getpid(); // use pid as a name of queue
    int msgflg = IPC_CREAT | 0666;
    if(!(msqid = msgget(key, msgflg)))
 	return -1;
    long resolution = DEF_RES;
    struct timespec w;
    if (clock_getres(CLOCK_REALTIME, &w) == 0) {
        resolution = (resolution > w.tv_nsec) ? resolution : w.tv_nsec;
    }
    w.tv_nsec = (GRANULARITY*resolution);
    long rep_interval = 1000000000L/w.tv_nsec;
    int counter = 0;
    long sync_wait = 0;
    struct tms prev_times;
    clock_t prev_clock = times(&prev_times);
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
            if (trace_sync) {
                struct syncmsg syncbuf = {
                    SYNCMSG,
                    (I32) sync_wait,
                    (I32) thr_count
                };
                msgsnd(msqid, &syncbuf, sizeof (syncbuf) - sizeof (syncbuf.type), IPC_NOWAIT);
            }
            if (trace_cpu) {
                struct tms cur_times;
                clock_t cur_clock = times(&cur_times);
                long delta = (long) (cur_clock - prev_clock);
                struct cpumsg cpubuf = {
                    CPUMSG,
                    (I32) (100.0 * (cur_times.tms_utime - prev_times.tms_utime) / delta),
                    (I32) (100.0 * (cur_times.tms_stime - prev_times.tms_stime) / delta)
                };
                msgsnd(msqid, &cpubuf, sizeof (cpubuf) - sizeof (cpubuf.type), IPC_NOWAIT);
                prev_clock = cur_clock;
                prev_times.tms_utime = cur_times.tms_utime;
                prev_times.tms_stime = cur_times.tms_stime;
            }
            if (trace_mem) {
                struct mallinfo mi = mall_hndl();
                struct memmsg membuf = {
                    MEMMSG,
                    mi.uordblks + mi.hblkhd
                };
                msgsnd(msqid, &membuf, sizeof (membuf) - sizeof (membuf.type), IPC_NOWAIT);
            }
            check_control(0);
        }
    }
    return 0;
}
