/* 
 * File:   pty_start.c
 * Author: ak119685
 *
 * Created on 22 ?????? 2010 ?., 12:32
 */

#include "pty_fork.h"
#include "loop.h"
#include "error.h"

#include <sys/termios.h>
#include <unistd.h>
#include <sys/wait.h>
#include <signal.h>
#include <fcntl.h>

#include <libgen.h>

#if defined __CYGWIN__ && !defined WCONTINUED
//added for compatibility with cygwin 1.5
#define WCONTINUED 0
#endif

extern int putenv(char *);

static void set_noecho(int);
const char* progname;

static void sigusr(int sig) {
}

/*
 * 
 */
int main(int argc, char** argv) {
    int noecho = 0;
    int waitSignal = 0;
    int master_fd = -1;
    int status = 0;
    int envnum = 0;
    int envsize = 0;
    char **envvars = NULL;
    char *pty = NULL;

    pid_t pid, w;

    int idx;
    int nopt = 1;

    progname = basename(argv[0]);

    for (idx = 1; idx < argc; idx++) {
        if (argv[idx][0] == '-') {
            if (strcmp(argv[idx], "-p") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing pty after -p\n");
                }
                pty = argv[idx];
                nopt += 2;
            } else if (strcmp(argv[idx], "--env") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing variable=value pair after --env\n");
                    exit(-1);
                }

                // Cannot put environment here as in case of fork these 
                // variables will affect us... 
                // Will do this only before real execv
                // putenv(argv[idx]);

                if (envsize == envnum) {
                    envsize += 10;
                    envvars = realloc(envvars, sizeof (char*) * envsize);
                }

                envvars[envnum++] = argv[idx];
                nopt += 2;
            } else if (strcmp(argv[idx], "-e") == 0) {
                noecho = 1;
                nopt += 1;
            } else if (strcmp(argv[idx], "-w") == 0) {
                waitSignal = 1;
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
        //  -e          turned echoing off
        //  -w          wait until signaled (USR1) before executing a process
        //  -p          define pts_name to use instead of opening a new one
        // --env        passes additional environment variable to a program
        //              in NAME=VALUE form. For multiple variables multiple
        //              --env options should be used.
        err_quit("usage: %s [-e] [-w] [-p pts_name] [[--env NAME=VALUE] ...] program [ arg ... ]\n"
                "\t-e\tturn echoing off\n"
                "\t-w\twait USR1 after reporting PID/TTY and before executing the program\n"
                "\t-p\tdefine pts_name to attach process's I/O instead of opening a new one\n"
                "\t--env\tpass (additional) environment variable to the process\n", progname);
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
        if (waitSignal) {
            signal(SIGUSR1, sigusr);
        }

        printf("PID=%d\n", getpid());
        printf("TTY=%s\n", pty == NULL ? "null" : pty);
        fflush(stdout);

        if (waitSignal) {
            pause();
        }

        if (noecho) {
            set_noecho(STDIN_FILENO);
        }

        // Set passed environment variables
        for (int i = 0; i < envnum; i++) {
            putenv(envvars[i]);
        }

        if (execvp(argv[0], argv) < 0) {
            err_sys("can't execute: %s", argv[0]);
        }
    }


    if (envvars != NULL) {
        free(envvars);
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

//static void usr1_handler(int signum) {
//    usr_interrupt = 1;
//}

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
