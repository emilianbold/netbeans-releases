/* 
 * File:   pty_start.c
 * Author: ak119685
 *
 * Created on 22 Апрель 2010 г., 12:32
 */

#include "pty_fork.h"
#include "loop.h"
#include "error.h"

#include <sys/termios.h>
#include <unistd.h>
#include <sys/wait.h>
#include <signal.h>
#include <fcntl.h>

static void set_noecho(int);

/*
 * 
 */
int main(int argc, char** argv) {
    int verbose = 0, noecho = 0;
    int master_fd = -1;
    int status;
    char *pty = NULL;

    pid_t pid, w;

    opterr = 0;

    int c;
    while ((c = getopt(argc, argv, "cevp:t:")) != EOF) {
        switch (c) {
            case 'e': /* noecho for slave pty's line discipline */
                noecho = 1;
                break;
            case 'v': /* verbose */
                verbose = 1;
                break;
            case 'p': /*pts name*/
                pty = optarg;
                break;
            case '?':
                err_quit("unrecognized option: -%c", optopt);
        }
    }

    if (optind >= argc) {
        err_quit("usage: pty_start [-eiv] [-p pts_name] program [ arg ... ]");
    }

    if (pty != NULL) {
        pid = pty_fork1(pty);
    } else {
        pid = pty_fork(&master_fd, &pty);
    }

    if (pid < 0) {
        err_sys("fork error");
    }

    if (pid == 0) { /* child */
        printf("%s\n", pty == NULL ? "null" : pty);
        fflush(stdout);

        if (noecho) {
            set_noecho(STDIN_FILENO);
        }

        if (execvp(argv[optind], &argv[optind]) < 0) {
            err_sys("can't execute: %s", argv[optind]);
        }
    }

    /* parent */

    if (master_fd > 0) {
        loop(master_fd); /* copies stdin -> ptym, ptym -> stdout */
        tcflush(master_fd, TCIOFLUSH);
        tcdrain(master_fd);
    }

    w = waitpid(pid, &status, WUNTRACED | WCONTINUED);

    if (w != -1 && WIFEXITED(status)) {
        exit(WEXITSTATUS(status));
    }

    exit(EXIT_FAILURE);
}

/**
 * turn off echo (for slave pty)
 */
static void set_noecho(int fd) {
    struct termios stermios;

    if (tcgetattr(fd, &stermios) < 0) {
        err_sys("tcgetattr error");
    }

    stermios.c_lflag &= ~(ECHO | ECHOE | ECHOK | ECHONL);

    /*
     * Also turn off NL to CR/NL mapping on output.
     */

    stermios.c_oflag &= ~(ONLCR);

    if (tcsetattr(fd, TCSANOW, &stermios) < 0) {
        err_sys("tcsetattr error");
    }
}
