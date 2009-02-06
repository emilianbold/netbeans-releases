
#include <stdio.h>

/*
 * File:   agent.c
 * Author: ll155635
 *
 * Created on January 29, 2009, 3:04 PM
 */
#ifdef __APPLE__
#include <malloc/malloc.h>
#else
#include <malloc.h>
#include <link.h>
#endif
#include <stdlib.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <dlfcn.h>

static int msqid;

#ifdef __APPLE__
struct mstats (*mstats_nhd)(void) = NULL;
char* mem_fun_name = "mstats";
#else
static struct mallinfo (*mall_hndl)(void) = NULL;
char* mem_fun_name = "mallinfo";
#endif


size_t minfo() {
#ifdef __APPLE__
    struct mstats ms = mstats();
    return ms.bytes_used;
#else
    struct mallinfo mi = mall_hndl();
    return mi.uordblks;
#endif
}

void report() {
    long buf = minfo();
    /* Send a message. */
    msgsnd(msqid, &buf, 0, IPC_NOWAIT);
}

void control_reporting(int i) {
    report();
    signal(SIGUSR1, control_reporting); /* set the control signal capture */
}

static void report_failure() {
    long buf = 0x7fffffff;
    /* Send a message. */
    msgsnd(msqid, &buf, 0, IPC_NOWAIT);
}

void control_reporting1(int i) {
    int have_mi = 0;
    // Let's check if there is a mallinfo function available
    void* hndl = dlopen(NULL, RTLD_GLOBAL);

#ifdef __APPLE__
    #define MEMFUN_ADDR mstats_nhd
#else
    #define MEMFUN_ADDR mall_hndl
#endif

    MEMFUN_ADDR = dlsym(hndl, mem_fun_name);

    if(MEMFUN_ADDR == NULL) {
        hndl = dlopen("/usr/lib/libmalloc.so", RTLD_LAZY);
        if(hndl)
            MEMFUN_ADDR = dlsym(hndl, mem_fun_name);
        if(MEMFUN_ADDR)
            have_mi = 1;
    }
    else
        have_mi =1;

    if(!have_mi)
        report_failure();
    else
        control_reporting(i); /* set the control signal capture */
}


void
__attribute__((constructor))
init_function(void) {
    /*test_dl();*/
    key_t key = getpid(); // use pid as a name of queue
    int msgflg = IPC_CREAT | 0666;

    /*
     * Get the message queue id for the
     * "pid" and create it
     */
    if ((msqid = msgget(key, msgflg )) < 0) {
        return;
    }

    // setitimer(ITIMER_REAL, &tout_val, 0);
    signal(SIGUSR1, control_reporting); /* set the control signal capture */
}

void
__attribute__ ((destructor))
fini_function (void) {
    if (msqid > 0) {
        int rc = msgctl (msqid, IPC_RMID, NULL);
    }
}
