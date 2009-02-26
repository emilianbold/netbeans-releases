#include <dlfcn.h>
#include <signal.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/types.h>

#include "instruments.h"

volatile int trace_sync = 0;
volatile static int thr_count = 1; // main thread is always here

//---- control and reporting section --------------------------

static int msqid = -1;
static void* hndl = NULL;

static void* reporter(void* arg);

static void start_reporting() {
    int tid;
    void* (*pcreate)(void*, void*, void*(*)(void*), void*) =  dlsym(hndl, "pthread_create");
    if(pcreate)
        pcreate(&tid, NULL, reporter, NULL);
}

static int init_sync_tracing();

static void report_failure() {
    struct syncmsg buf = {
        SYNCMSG,
        FAILURE,
        1
    };
    msgsnd(msqid, &buf, sizeof(buf) - sizeof(buf.type), IPC_NOWAIT);
}

static int pthreads_enabled = 0;

static void control_reporting(int i) {

    if(msqid < 0)
        return;

    if(!pthreads_enabled) {
        report_failure();
    } else {
        trace_sync = !trace_sync;
    }
    signal(SIGUSR2, control_reporting); /* set the control signal capture */
}

void cleanup(void* p) {
    printf("cleanup\n");
    if(msqid >= 0) {
        struct msqid_ds msbuf;
        msgctl(msqid, IPC_RMID, &msbuf); // closing the queue
        msqid = 0;
    }
    //printf("sync waits took %lf sec\n", ((double)sync_wait)/CLOCKS_PER_SEC);
}

void
__attribute((constructor))
init_function(void) {
    if((pthreads_enabled = init_sync_tracing()))
        start_reporting();
    key_t key = getpid(); // use pid as a name of queue
    int msgflg = IPC_CREAT | 0666;

    /*
     * Get the message queue id for the
     * "pid" and create it
     */
    msqid = msgget(key, msgflg);
    signal(SIGUSR2, control_reporting); /* set the control signal capture */
    // init_sync_tracing(); // JUST FOR TESTING!!!!
}


void
__attribute((destructor))
fini_function(void) {
    cleanup(0);
}

//---- tracing section ---------------------------------------------

#ifdef __i386__
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
static int (* ORIG(func))(void* p param) = NULL; \
int func (void * p param) { \
    thlock++; \
    int ret = ORIG(func) (p actual); \
    thlock--; \
    return ret; \
}

static __thread int thlock = 0;
static __thread int tid;

#define INIT(func) \
    ORIG(func) = dlsym(hndl, QUOTE(func))

INSTRUMENT(pthread_cond_wait, VOID1P, ACTUAL1)
INSTRUMENT(pthread_cond_timedwait, VOID2P, ACTUAL2)
INSTRUMENT(pthread_mutex_lock, , )
INSTRUMENT(pthread_mutex_setprioceiling, INT2P, ACTUAL2)
INSTRUMENT(pthread_rwlock_rdlock, , )
INSTRUMENT(pthread_rwlock_wrlock, , )
INSTRUMENT(pthread_barrier_wait, , )
INSTRUMENT(pthread_join, VOID1P, ACTUAL1)
INSTRUMENT(pthread_mutex_timedlock, VOID1P, ACTUAL1)
INSTRUMENT(pthread_rwlock_timedrdlock, VOID1P, ACTUAL1)
INSTRUMENT(pthread_rwlock_timedwrlock, VOID1P, ACTUAL1)
INSTRUMENT(pthread_spin_lock, , )


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
}

int pthread_create(void *newthread,
                   void *attr,
		   void *(*user_start_routine) (void *),
		   void *arg)
{
    start_pkg * user_data = malloc(sizeof(start_pkg));
    user_data->entry_point = user_start_routine;
    user_data->arg = arg;
    return ORIG(pthread_create)(newthread, attr, start_routine, user_data);
}

int init_sync_tracing() {
    if((hndl = dlopen("libpthread.so.0", RTLD_LAZY))) {

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

void* reporter(void* arg) {
    long resolution = 10000;
    struct timespec w;
    if(clock_getres(CLOCK_REALTIME, &w))
        resolution = w.tv_nsec;
    w.tv_nsec = (GRANULARITY*resolution);
    long rep_interval = 1000000000L/w.tv_nsec;
    int counter = 0;
    long sync_wait = 0;
    struct timespec rem;
//    trace_sync = 1;
    int i = 0;

    if(ORIG(pthread_cleanup_push))
        ORIG(pthread_cleanup_push)(cleanup, 0);

    while (1) {
        nanosleep(&w, NULL);

//        lock();
        long lock_count = 0;
        for(i = 0; i < MAXTHR; i++) {
            if(flags[i] && *(flags[i]))
                lock_count++;
        }
//        unlock();
        sync_wait += lock_count;

        if (trace_sync && (counter++ > rep_interval)) {
            counter = 0;
            struct syncmsg buf = {
                SYNCMSG,
                sync_wait,
                thr_count
            };
            msgsnd(msqid, &buf, sizeof(buf) - sizeof(buf.type), IPC_NOWAIT);
            //printf("%ld\n", sync_wait);
        }
    }
    return NULL;
}



