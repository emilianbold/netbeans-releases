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

#ifndef CLK_TCK
#define CLK_TCK (sysconf(_SC_CLK_TCK))
#endif

static int user_term = 0;

void terminate(int i) {
    user_term = 1;
}

int main(int argc, char** argv) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s flags pid\n", argv[0]);
        fprintf(stderr, "Flags:\n");
        fprintf(stderr, "\t-c  CPU monitoring\n");
        fprintf(stderr, "\t-m  memory monitoring\n");
        fprintf(stderr, "\t-s  synchronization monitoring\n");
        return 1;
    }

    int monitor_cpu = 0;
    int monitor_mem = 0;
    int monitor_sync = 0;
	int i, j;
    for (i = 1; i + 1 < argc; ++i) {
        if (argv[i][0] == '-') {
            for (j = 1; argv[i][j]; ++j) {
                switch (argv[i][j]) {
                    case 'c':
                        monitor_cpu = 1;
                        break;
                    case 'm':
                        monitor_mem = 1;
                        break;
                    case 's':
                        monitor_sync = 1;
                        break;
                    default:
                        fprintf(stderr, "Unknown flag %c\n", argv[i][j]);
                        return 1;
                }
            }
        } else {
            fprintf(stderr, "Unknown flag %s\n", argv[i]);
            return 1;
        }
    }

    if (monitor_cpu + monitor_mem + monitor_sync == 0) {
        fprintf(stderr, "No flags specified, exiting\n");
        return 1;
    }

    long pid = strtol(argv[argc - 1], 0, 10);
    int msqid = -1;

    if (pid) {
        // Get the message queue id for the "pid"
        msqid = msgget((int) pid, IPC_CREAT | 0666);
    }
    if (msqid < 0) {
        fprintf(stderr, "Can not create IPC channel to process %s\n", argv[1]);
        return -2;
    }

    struct timespec res;
    long resolution = DEF_RES;
    if (clock_getres(CLOCK_REALTIME, &res) == 0) {
        resolution = (resolution > res.tv_nsec) ? resolution : res.tv_nsec;
    }
    long per_sec = 1000000000L / (GRANULARITY * resolution);

    signal(SIGINT, terminate);

    struct memmsg membuf = {MEMMSG, 0};
    struct syncmsg syncbuf = {SYNCMSG, 0, 0};
    struct cpumsg cpubuf = {CPUMSG, 0, 0};
    struct ctrlmsg ctrl = {CTRLMSG, 0xf, 0xf};
    if (msgsnd(msqid, &ctrl, sizeof (ctrl) - sizeof(ctrl.type), IPC_NOWAIT) < 0) {
        fprintf(stderr, "Handshake with process %s failed\n", argv[1]);
        return -4;
    }

    int silence = 0;
    while (1) {
        int numget = monitor_cpu + monitor_mem + monitor_sync;
        if (monitor_sync) {
            if (msgrcv(msqid, &syncbuf, sizeof (syncbuf) - sizeof (syncbuf.type), SYNCMSG, IPC_NOWAIT) < 0) {
                if (user_term || errno != ENOMSG) {
                    break;
                } else {
                    numget--;
                }
            } else {
                printf("sync: %lf\t%d\n",
                        ((double) syncbuf.lock_ticks) / per_sec,
                        syncbuf.thr_count
                        );
                        fflush(stdout);
            }
        }
        if (monitor_mem) {
            if (msgrcv(msqid, &membuf, sizeof (membuf) - sizeof (membuf.type), MEMMSG, IPC_NOWAIT) < 0) {
                if (user_term || errno != ENOMSG) {
                    break;
                } else {
                    numget--;
                }
            } else {
                printf("mem: %d\n", membuf.heapused);
                fflush(stdout);
            }
        }
        if (monitor_cpu) {
            if (msgrcv(msqid, &cpubuf, sizeof (cpubuf) - sizeof (cpubuf.type), CPUMSG, IPC_NOWAIT) < 0) {
                if (user_term || errno != ENOMSG) {
                    break;
                } else {
                    numget--;
                }
            } else {
                printf("cpu: %d\t%d\n", cpubuf.user, cpubuf.sys);
                fflush(stdout);
            }
        }
        if (numget) {
            silence = 0;
        } else {
            struct failmsg fbuf;
            if (msgrcv(msqid, &fbuf, sizeof (fbuf) - sizeof (fbuf.type), FAILMSG, IPC_NOWAIT) == 0) {
                if (fbuf.type == MEMMSG) {
                    monitor_mem = 0;
                    printf("mem: failure!\n");
                }
                if (fbuf.type == CPUMSG) {
                    monitor_cpu = 0;
                    printf("cpu: failure!\n");
                }
                if (fbuf.type == SYNCMSG) {
                    monitor_sync = 0;
                    printf("sync: failure!\n");
                }
            };
            if (silence > 3) {
                struct msqid_ds msbuf;
                msgctl(msqid, IPC_RMID, &msbuf); // looks like agent has been killed and queue remains alive
                fprintf(stderr, "Communication with process %s has been lost\n", argv[1]);
                return -4;
            }
            silence++;
            sleep(1);
        }
    }
    if (user_term) {
        ctrl.action = 0;
        msgsnd(msqid, &ctrl, sizeof (ctrl) - sizeof(ctrl.type), IPC_NOWAIT);
    }

    return 0;
}
