#include <stdio.h>
#include <signal.h>
#include <sys/ipc.h>

#include "dlight.h"

#if DEBUG
#define dbg_log(text, arg1, arg2, arg3) fprintf(stderr, text, (arg1), (arg2), (arg3))
#else
#define dbg_log(text, arg1, arg2, arg3)
#endif

int process(long pid, int msqid) {
    if (kill(pid, SIGUSR1) < 0) {
        fprintf(stderr, "Can not communicate to process %d (error sending a signal)\n", pid);
        return -3;
    }

    struct dlight_msg_mem buf;
    buf.type = DLIGHT_MEM;
    if (msgrcv(msqid, &buf, sizeof(buf.heap_used), 0, 0) < 0) {
        fprintf(stderr, "Communication with process %d has been lost (1)\n", pid);
        return -4;
    }
    if (buf.heap_used == DLIGHT_ERROR) {
        fprintf(stderr, "Agent reported an error\n");
        return -5;
    } else {
        printf("%ld\n", buf.heap_used);
        sleep(1);
    }

    while (1) {
        dbg_log("sending SIGUSR1 to %d ...\n", pid, 0, 0);
        if (kill(pid, SIGUSR1) < 0) {
            fprintf(stderr, "Communication with process %d has been lost (2)\n", pid);
            return -3;
        }

        dbg_log("receiving from queue %d\n", msqid, 0, 0);
        if (msgrcv(msqid, &buf, sizeof(buf.heap_used), 0, 0) < 0) {
            fprintf(stderr, "Communication with process %d has been lost (3)\n", pid);
            return -4;
        }
        dbg_log("%ld\n", buf.heap_used, 0, 0);
        fprintf(stdout, "%ld\n", buf.heap_used);
        fflush(stdout);
        sleep(1);
    }
}

int main(int argc, char** argv) {
    if (argc < 2) {
        fprintf(stderr, "PID not specified\n");
        return -1;
    }
    long pid = strtol(argv[1], 0, 10);
    key_t key = (key_t) pid;
    //key = ftok(argv[0], 1);

    int msqid = -1;

    if (key) {
        /* Get the message queue id for the "pid" */
        msqid = msgget((int) key, IPC_CREAT | 0666);
    }
    if (msqid < 0) {
        fprintf(stderr, "Can not communicate to process %d (error creating message queue %d)\n", pid, key);
        return -2;
    }
    dbg_log("Got message queue %d for key %d for communicatig with %d ...\n", msqid, key, pid);

    int rc = process(pid, msqid);

    if (msqid > 0) {
        int rc = msgctl (msqid, IPC_RMID, NULL);
        dbg_log("removing queue id=%d rc=%d", msqid, rc, 0);
    }

    return rc;
}
