#include "loop.h"
#include "error.h"
#include <poll.h>
#include <sys/termios.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#ifndef INFTIM
#define INFTIM  -1
#endif


static ssize_t writen(int filedes, const void *buf, size_t nbytes);

#ifdef __APPLE__

int loop(int master_fd) {
    ssize_t n;
    char buf[BUFSIZ];
    int select_result;
    fd_set read_set;

    for (;;) {
        FD_ZERO(&read_set);
        FD_SET(STDIN_FILENO, &read_set);
        FD_SET(master_fd, &read_set);
        select_result = select(master_fd + 1, &read_set, NULL, NULL, NULL);

        // interrupted select is ignored - see CR 7086177
        if (select_result == -1 && errno == EINTR) {
            continue;
        }
        
        if (select_result == -1) {
            err_sys("poll failed\n");
        }

        if (FD_ISSET(STDIN_FILENO, &read_set)) {
            if ((n = read(STDIN_FILENO, buf, BUFSIZ)) == -1) {
                err_sys("read from stdin failed\n");
            }

            if (n == 0) {
                break;
            }

            if (write(master_fd, buf, n) == -1) {
                err_sys("write to master failed\n");
            }
        }

        if (FD_ISSET(master_fd, &read_set)) {
            if ((n = read(master_fd, buf, BUFSIZ)) == -1) {
                err_sys("read from master failed\n");
            }

            if (n == 0) {
                break;
            }

            if (write(STDOUT_FILENO, buf, n) == -1) {
                err_sys("write to stdout failed\n");
                exit(1);
            }
        }
    }

    return 0;
}

#else

int loop(int master_fd) {
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

        // interrupted poll is ignored - see CR 7086177
        if (poll_result == -1 && errno == EINTR) {
            continue;
        }

        if (poll_result == -1) {
            err_sys("poll() failed in main_loop");
        }

        if (fds[0].revents & POLLIN) {
            if ((n = read(STDIN_FILENO, buf, BUFSIZ)) == -1) {
                err_sys("read from stdin failed");
            }

            if (n == 0) {
#ifdef __CYGWIN__
                // On Windows when calling process is killed,
                // POLLIN flag is set, not POLLHUP.
                // So behave as if we have received POLLHUP in this case...
                close(master_fd);
                return 1;
#endif
                break;
            }

            if (writen(master_fd, buf, n) == -1) {
                err_sys("write to master failed\n");
            }
        }

        if (fds[1].revents & POLLIN) {
            if ((n = read(master_fd, buf, BUFSIZ)) == -1) {
                err_sys("read from master failed\n");
            }

            if (n == 0) {
                break;
            }

            if (writen(STDOUT_FILENO, buf, n) == -1) {
                err_sys("write to stdout failed\n");
            }
        }

        if (fds[1].revents & POLLHUP) {
            break;
        }

        if (fds[0].revents & POLLHUP) {
            // STDIN END is broken... 
            // Cannot just break at this point as 'child' process still alive.
            // [will hung on waitpid later.. ]
            // So will close the MASTER END => this causes a hangup to occur on 
            // the other end of the pipe.
            // no data is drained in this case...
            close(master_fd);
            return 1;
        }
    }

    return 0;
}
#endif

static ssize_t writen(int fd, const void *ptr, size_t n) {
    const char *pos = ptr;
    size_t nleft = n;
    ssize_t nwritten;

    while (nleft > 0) {
        if ((nwritten = write(fd, pos, nleft)) < 0) {
            if (nleft == n)
                return (-1); /* error, return -1 */
            else
                break; /* error, return amount written so far */
        } else if (nwritten == 0) {
            break;
        }
        nleft -= nwritten;
        pos += nwritten;
    }
    return (n - nleft); /* return >= 0 */
}
