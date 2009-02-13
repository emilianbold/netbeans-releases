/*
 * File:   agent.c
 * Author: ll155635
 */
#include <stdio.h>
#include <stdlib.h>

#ifdef __APPLE__
#include <malloc/malloc.h>
#else
#include <malloc.h>
#include <link.h>
#endif

#include <signal.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <dlfcn.h>
#include <errno.h>

#include "dlight.h"

static int msqid;

#if DEBUG
static FILE *logfile;
#define dbg_log(text, arg1, arg2, arg3) { fprintf(logfile, text, (arg1), (arg2), (arg3)); fflush(logfile); }
#else
#define dbg_log(text, arg1, arg2, arg3)
#endif

#ifdef __APPLE__
struct mstats (*mstats_nhd)(void) = NULL;
static char* mem_fun_name = "mstats";
#define MEMFUN_ADDR mstats_nhd
#else
static struct mallinfo (*mall_hndl)(void) = NULL;
static char* mem_fun_name = "mallinfo";
#define MEMFUN_ADDR mall_hndl
#endif

static size_t minfo() {
#ifdef __APPLE__
    struct mstats ms = mstats();
    return ms.bytes_used;
#else
    struct mallinfo mi = mall_hndl();
    return mi.uordblks;
#endif
}

static void report() {
    if (MEMFUN_ADDR) {
        struct dlight_msg_mem buf;
        buf.type = DLIGHT_MEM;
        buf.heap_used = minfo();
        /* Send a message. */
        dbg_log("\nreporting: %ld bytes msgtype %d msgsize=%d\n", buf.heap_used, buf.type, sizeof(buf.heap_used));
        int rc = msgsnd(msqid, &buf, sizeof(buf.heap_used), IPC_NOWAIT);
        dbg_log("reported: %ld rc=%d errno=%d\n\n", buf.heap_used, rc, errno);
    } else {
        dbg_log("can't report: function not found", 0, 0, 0);
    }
}

static void control_reporting(int i) {
    report();
    signal(SIGUSR1, control_reporting); /* set the control signal capture */
}

static void report_failure() {
    dbg_log("report_failure\n", 0, 0, 0);
    struct dlight_msg_mem buf;
    buf.type = DLIGHT_MEM;
    buf.heap_used = DLIGHT_ERROR;
    /* Send a message. */
    msgsnd(msqid, &buf, sizeof(buf.heap_used), IPC_NOWAIT);
}

static void control_reporting_first(int i) {
    int have_mi = 0;
    // Let's check if there is a mallinfo function available
    void* hndl = dlopen(NULL, RTLD_GLOBAL);

    MEMFUN_ADDR = dlsym(hndl, mem_fun_name);

    if(MEMFUN_ADDR == NULL) {
        dbg_log("control_reporting_first: 1-st attempt failed \n", 0, 0, 0);
        hndl = dlopen("/usr/lib/libmalloc.so", RTLD_LAZY);
        if(hndl) {
            MEMFUN_ADDR = dlsym(hndl, mem_fun_name);
        }
        if(MEMFUN_ADDR) {
            have_mi = 1;
            dbg_log("control_reporting_first: %s found\n", mem_fun_name, 0, 0);
        } else {
            dbg_log("control_reporting_first: %s not found\n", mem_fun_name, 0, 0);
        }
    }
    else {
        have_mi =1;
    }

    if(!have_mi)
        report_failure();
    else
        control_reporting(i); /* set the control signal capture */
}


void
__attribute__((constructor))
init_function(void) {
    #if DEBUG
    #if DEBUG > 1
    logfile = stderr;
    #else
    logfile = fopen("/tmp/magent.log", "wa");
    #endif
    fprintf(logfile, "\n\n--------------------\n");
    #endif

    key_t key = getpid(); // use pid as a name of queue

    /* Get the message queue id for the "pid" and create it */
    if ((msqid = msgget(key, IPC_CREAT | 0666)) < 0) {
        dbg_log("error creating message queue key=%d (%X)\n", key, key, 0);
        return;
    } else {
        dbg_log("succeeded creating message queue key=%d (%X), id=%d\n", key, key, msqid);
    }

    // setitimer(ITIMER_REAL, &tout_val, 0);
    signal(SIGUSR1, control_reporting_first); /* set the control signal capture */
}

void
__attribute__ ((destructor))
fini_function (void) {
    dbg_log("dtor\n", 0, 0, 0);
    if (msqid > 0) {
        int rc = msgctl (msqid, IPC_RMID, NULL);
        dbg_log("removing queue id=%d rc=%d", msqid, rc, 0);
    }
    #if DEBUG
    fclose(logfile);
    #endif
}
