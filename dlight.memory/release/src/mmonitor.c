#include <stdio.h>
#include <signal.h>

int main(int argc, char** argv) {
    if (argc < 2) {
        fprintf(stderr, "PID not specified\n");
        return -1;
    }
    long pid = strtol(argv[1], 0, 10);

    int msqid = -1;

    if (pid) {
        /* Get the message queue id for the "pid" */
        msqid = msgget((int) pid, 0666);
    }
    if (msqid < 0) {
        fprintf(stderr, "Can not communicate to process %s\n", argv[1]);
        return -2;
    }

    if (kill(pid, SIGUSR1) < 0) {
        fprintf(stderr, "Can not communicate to process %s\n", argv[1]);
        return -3;
    }

    long first;
    if (msgrcv(msqid, &first, 0, 0, 0) < 0) {
        fprintf(stderr, "Communication with process %s has been lost\n", argv[1]);
        return -4;
    }
    if (first == 0x7fffffff) {
        fprintf(stderr, "Agent reported an error\n");
        return -5;
    } else {
        printf("%ld\n", first);
        sleep(1);
    }
    while (1) {
        if (kill(pid, SIGUSR1) < 0) {
            fprintf(stderr, "Communication with process %s has been lost\n", argv[1]);
            return -3;
        }
        long buf;
        if (msgrcv(msqid, &buf, 0, 0, 0) < 0) {
            fprintf(stderr, "Communication with process %s has been lost\n", argv[1]);
            return -4;
        }
        printf("%ld\n", buf);
        sleep(1);
    }
    return 0;
}
