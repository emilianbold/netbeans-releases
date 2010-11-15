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

#if defined __CYGWIN__ && !defined WCONTINUED
//added for compatibility with cygwin 1.5
#define WCONTINUED 0
#endif

static void set_noecho(int);

/*
 * 
 */
int main(int argc, char** argv) {
    int noecho = 0;
    int master_fd = -1;
    int status;
    char *pty = NULL;

    pid_t pid, w;

    int idx;
    int nopt = 1;

    for (idx = 1; idx < argc; idx++) {
        if (argv[idx][0] == '-') {
            if (strcmp(argv[idx], "-p") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    printf("ERROR missing pty after -p\n");
                    exit(-1);
                }
                pty = argv[idx];
                nopt += 2;
            } else if (strcmp(argv[idx], "-e") == 0) {
                noecho = 1;
                nopt += 1;
            } else {
                printf("ERROR unrecognized option '%s'\n", argv[idx]);
                exit(-1);
            }
        } else {
            break;
        }
    }

    argv += nopt;
    argc -= nopt;
    /* now argv points to the executable */

    if (argc == 0) {
        err_quit("usage: pty_start [-e] [-p pts_name] program [ arg ... ]");
        exit(-1);
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
        printf("PID=%d\n", getpid());
        printf("TTY=%s\n", pty == NULL ? "null" : pty);
        fflush(stdout);

        if (noecho) {
            set_noecho(STDIN_FILENO);
        }

        if (execvp(argv[0], argv) < 0) {
            err_sys("can't execute: %s", argv[0]);
        }
    }

    /* parent */

    int loop_result = 0;

    if (master_fd > 0) {
        // At least on Windows, when gdb is started through this pty process
        // and calling process is killed (i.e. stdin gets broken)
        // and even when we close master_fd, gdb continues to work... 
        // ??? will kill the process (gdb) in this case...
        loop_result = loop(master_fd); /* copies stdin -> ptym, ptym -> stdout */
    }

    if (loop_result != 0) {
        int attempt = 2;
        while (attempt-- >= 0 && kill(pid, 0) == 0) {
            kill(pid, SIGTERM);
            sleep(1);
        }

        if (kill(pid, 0) == 0) {
            kill(pid, SIGKILL);
        }
    }

    w = waitpid(pid, &status, WUNTRACED | WCONTINUED);

    if (master_fd > 0) {
        tcdrain(master_fd);
    }

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
