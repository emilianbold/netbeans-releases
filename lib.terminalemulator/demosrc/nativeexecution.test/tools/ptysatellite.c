/*
 * File:   ptysatelite.c
 * Author: ak119685
 *
 */

#if !defined (__SVR4) || !defined (__sun)
#define _XOPEN_SOURCE 600
#define _BSD_SOURCE
#include <termios.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <stropts.h>
#include <unistd.h>
#include <fcntl.h>
#include <poll.h>

static int ptm_open(void) {
    int masterfd;

    if ((masterfd = posix_openpt(O_RDWR)) == -1) {
        return -1;
    }

    if (grantpt(masterfd) == -1 || unlockpt(masterfd) == -1) {
        close(masterfd);
        return -1;
    }

    return masterfd;
}

static int pts_open(int masterfd) {
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
#else
    /*
        struct termios termios_p;
        tcgetattr(slavefd, &termios_p);
        cfmakeraw(&termios_p);
        tcsetattr(slavefd, TCSANOW, &termios_p);
     */
#endif

    return slavefd;
}

static void loop(int master_fd) {
    ssize_t n;
    char buf[BUFSIZ];
    struct pollfd fds[2];

    fds[0].fd = STDIN_FILENO;
    fds[0].events = POLLIN;
    fds[0].revents = 0;
    fds[1].fd = master_fd;
    fds[1].events = POLLIN;
    fds[1].revents = 0;

    int poll_result;

    for (;;) {
        poll_result = poll((struct pollfd*) & fds, 2, /*INFTIM*/ -1);

        if (poll_result == -1) {
            printf("ERROR: poll failed\n");
            exit(1);
        }

        if (fds[0].revents & POLLHUP || fds[1].revents & POLLHUP) {
            break;
        }

        if (fds[0].revents & POLLIN) {
            if ((n = read(STDIN_FILENO, buf, BUFSIZ)) == -1) {
                printf("ERROR: read from stdin failed\n");
                exit(1);
            }

            if (n == 0) {
                break;
            }

            if (write(master_fd, buf, n) == -1) {
                printf("ERROR: write to master failed\n");
                exit(1);
            }
        }

        if (fds[1].revents & POLLIN) {
            if ((n = read(master_fd, buf, BUFSIZ)) == -1) {
                printf("ERROR: read from master failed\n");
                exit(1);
            }

            if (n == 0) {
                break;
            }

            if (write(STDOUT_FILENO, buf, n) == -1) {
                printf("ERROR: write to stdout failed\n");
                exit(1);
            }
        }
    }
}

int main(int argc, char** argv) {
    int master_fd;
    int slave_fd;
    char* name;

    if ((master_fd = ptm_open()) == -1) {
        printf("ERROR: ptm_open() failed\n");
        exit(1);
    }

    if ((name = ptsname(master_fd)) == NULL) {
        close(master_fd);
        return -1;
    }

    // open slave to setup line discipline...
    if ((slave_fd = pts_open(master_fd)) == -1) {
        printf("ERROR: cannot open PTY slave\n");
        exit(1);
    }

    printf("PID: %d\n", getpid());
    printf("TTY: %s\n\n", name);
    fflush(stdout);

    loop(master_fd);

    return (EXIT_SUCCESS);
}
