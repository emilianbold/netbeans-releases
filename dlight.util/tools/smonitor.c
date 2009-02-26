#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>

#include "instruments.h"

static int user_term = 0;

void terminate(int i) {
    user_term = 1;
}

int main(int argc, char** argv) {
    if (argc < 2) {
        fprintf(stderr, "PID not specified\n");
        return -1;
    }
    long pid = strtol(argv[1], 0, 10);

    int msqid = -1;

    if (pid) {
        /*
         * Get the message queue id for the
         * "pid"
         */
        msqid = msgget((int) pid, 0666);
    }
    if (msqid < 0) {
        fprintf(stderr, "Can not communicate to process %s\n", argv[1]);
        return -2;
    }

    if (kill(pid, SIGUSR2) < 0) {
        fprintf(stderr, "Can not communicate to process %s\n", argv[1]);
        return -3;
    }

    signal(SIGINT, terminate);

    struct syncmsg buf = { SYNCMSG, 0, 0 };
    if (msgrcv(msqid, &buf, sizeof(buf) - sizeof(buf.type), SYNCMSG, 0) < 0) {
        fprintf(stderr, "Communication with process %s has been lost\n", argv[1]);
        return -4;
    }
    if(buf.lock_ticks == FAILURE) {
        fprintf(stderr, "Agent reported an error\n");
        return -5;
    }
    struct timespec res;
    long resolution = 10000;
    if(clock_getres(CLOCK_REALTIME, &res))
        resolution = res.tv_nsec;
    long per_sec = 1000000000L/(GRANULARITY*resolution);

    printf("sync_waits\t#threads\n");
    printf("%lf\t\t%d\n", ((double)buf.lock_ticks)/per_sec, buf.thr_count);
    int silence = 0;
    while (1) {
        if (msgrcv(msqid, &buf, sizeof(buf) - sizeof(buf.type), SYNCMSG, IPC_NOWAIT) < 0) {
            if(errno == ENOMSG) {
                if(silence > 3) {
                    struct msqid_ds msbuf;
                    msgctl(msqid, IPC_RMID, &msbuf); // looks like agent has been killed and queue remains alive
                    fprintf(stderr, "Communication with process %s has been lost\n", argv[1]);
                    return -4;
                }
                silence++;
                sleep(1);
            }
        }
        else {
            printf("%lf\t\t%d\n", ((double)buf.lock_ticks)/per_sec, buf.thr_count);
            silence = 0;
        }
        if(user_term) {
            kill(pid, SIGUSR2);
            break;
        }
    }
    return 0;
}
