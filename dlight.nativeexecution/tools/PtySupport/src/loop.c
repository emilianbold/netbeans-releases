#include "loop.h"
#include <poll.h>
#include <sys/termios.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/stropts.h>


static ssize_t writen(int filedes, const void *buf, size_t nbytes);

void loop(int master_fd) {
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
        poll_result = poll((struct pollfd*) & fds, 2, INFTIM);

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

            if (writen(master_fd, buf, n) == -1) {
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

            if (writen(STDOUT_FILENO, buf, n) == -1) {
                printf("ERROR: write to stdout failed\n");
                exit(1);
            }
        }
    }
}

static ssize_t writen(int fd, const void *ptr, size_t n) {
    size_t nleft;
    ssize_t nwritten;
    nleft = n;
    while (nleft > 0) {
        if ((nwritten = write(fd, ptr, nleft)) < 0) {
            if (nleft == n)
                return (-1); /* error, return -1 */
            else
                break; /* error, return amount written so far */
        } else if (nwritten == 0) {
            break;
        }
        nleft -= nwritten;
        ptr += nwritten;
    }
    return (n - nleft); /* return >= 0 */
}
