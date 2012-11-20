#include "util.h"

ssize_t writen(int fd, const void *ptr, size_t n) {
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
