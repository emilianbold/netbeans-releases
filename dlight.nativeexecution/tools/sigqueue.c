#include <stdio.h>
#include <stdlib.h>
#include <signal.h>


void usage() {
    fprintf(stderr, "Usage: \n");
    fprintf(stderr, "   ./sigqueue pid signo value\n");
}

/*
 * CLI wrapper for sigqueue.
 * Usage:
 *    sigqueue pid signo value
 *
 */

int main(int argc, char** argv) {
    if (argc < 4) {
        usage();
        exit(1);
    }

    //setting pid
    pid_t pid = atoi(argv[1]);

    //setting signo
    int signo = atoi(argv[2]);

    // setting value
    union sigval value;
    value.sival_int = atoi(argv[3]);

    return sigqueue(pid, signo, value);
}

