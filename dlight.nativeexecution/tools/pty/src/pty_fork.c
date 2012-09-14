#include "pty_fork.h"
#include "error.h"

#include <unistd.h>
#include <termios.h>
#include <stdarg.h>
#if !defined __APPLE__ && !defined __CYGWIN__
#include <stropts.h>
#else
#include <sys/ioctl.h>
#endif
#include <errno.h>
#include <fcntl.h>
#include <signal.h>

#ifdef __CYGWIN__
//added for compatibility with cygwin 1.5

int posix_openpt(int flags) {
    return open("/dev/ptmx", flags);
}

#endif
extern int  grantpt(int);
extern int  unlockpt(int);
extern char *ptsname(int);

static void dup_fd(int pty_fd);
static int ptm_open(void);
static int pts_open(int masterfd);

int ptm_open(void) {
    int masterfd;

    if ((masterfd = posix_openpt(O_RDWR | O_NOCTTY)) == -1) {
        return -1;
    }

    if (grantpt(masterfd) == -1 || unlockpt(masterfd) == -1) {
        close(masterfd);
        return -1;
    }

    return masterfd;
}

int pts_open(int masterfd) {
    int slavefd;
    char* name;

    if ((name = ptsname(masterfd)) == NULL) {
        close(masterfd);
        return -1;
    }

    if ((slavefd = open(name, O_RDWR)) == -1) {
        close(masterfd);
        return -1;
    }

#if defined (__SVR4) && defined (__sun)
    if (ioctl(slavefd, I_PUSH, "ptem") == -1) {
        close(masterfd);
        close(slavefd);
        return -1;
    }

    if (ioctl(slavefd, I_PUSH, "ldterm") == -1) {
        close(masterfd);
        close(slavefd);
        return -1;
    }

    if (ioctl(slavefd, I_PUSH, "ttcompat") == -1) {
        close(masterfd);
        close(slavefd);
        return -1;
    }
#endif

    return slavefd;
}

pid_t pty_fork(int *ptrfdm, char** pts_name) {
    pid_t pid;
    char* name;
    int master_fd, pty_fd;

    if ((master_fd = ptm_open()) < 0) {
        err_sys("ERROR: ptm_open() failed [%d]\n", master_fd);
    }

    if ((name = ptsname(master_fd)) == NULL) {
        close(master_fd);
        return -1;
    }

    // Put values to the output params
    *pts_name = name;
    *ptrfdm = master_fd;

    if ((pid = fork()) < 0) {
        printf("FAILED");
        return (-1);
    }

    if (pid == 0) { /* child */
        if (setsid() < 0) {
            err_sys("setsid error");
        }

        if ((pty_fd = pts_open(master_fd)) < 0) {
            err_sys("can't open slave pty");
        }

        close(master_fd);
        dup_fd(pty_fd);

        return (0); /* child returns 0 just like fork() */
    } else { /* parent */
        return (pid); /* parent returns pid of child */
    }

}

pid_t pty_fork1(char *pty) {
    pid_t pid;
    int pty_fd;

    if ((pid = fork()) < 0) {
        printf("FAILED");
        return (-1);
    }

    if (pid == 0) { /* child */
        if (setsid() < 0) {
            err_sys("setsid error");
        }

        if ((pty_fd = open(pty, O_RDWR)) == -1) {
            err_sys("ERROR cannot open pty \"%s\" -- %s\n",
                    pty, strerror(errno));
        }

        dup_fd(pty_fd);
        return (0);
    } else {
        return (pid); /* parent returns pid of child */
    }
}

static void dup_fd(int pty_fd) {
    // Ensure SIGINT isn't being ignored
    struct sigaction act;
    sigaction(SIGINT, NULL, &act);
    act.sa_handler = SIG_DFL;
    sigaction(SIGINT, &act, NULL);


#if defined(TIOCSCTTY) && !defined(__sun)
    if (ioctl(pty_fd, TIOCSCTTY, 0) == -1) {
        printf("ERROR ioctl(TIOCSCTTY) failed on \"pty %d\" -- %s\n",
                pty_fd, strerror(errno));
        exit(-1);
    }
#endif

    /*
     * Slave becomes stdin/stdout/stderr of child.
     */
    if (dup2(pty_fd, STDIN_FILENO) != STDIN_FILENO) {
        err_sys("dup2 error to stdin");
    }

    if (dup2(pty_fd, STDOUT_FILENO) != STDOUT_FILENO) {
        err_sys("dup2 error to stdout");
    }

    if (dup2(pty_fd, STDERR_FILENO) != STDERR_FILENO) {
        err_sys("dup2 error to stderr");
    }

    close(pty_fd);
}
