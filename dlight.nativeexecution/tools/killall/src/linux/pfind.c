#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include "../pfind.h"

#define MAX_LEN 4048

static char *procdir = "/proc";

pid_t* pfind(const char* magicenv) {
    pid_t *result = NULL;
    char fname[PATH_MAX];
    char buffer[MAX_LEN];
    DIR *dirp;
    struct dirent *dentp;
    int fd, res = 0, ressize = 0;

    if ((dirp = opendir(procdir)) == NULL) {
        return NULL;
    }

    while (dentp = readdir(dirp)) {
        if (dentp->d_name[0] < '0' || dentp->d_name[0] > '9') {
            continue;
        }

        char* pid = dentp->d_name;

        snprintf(fname, PATH_MAX, "%s/%s/environ", procdir, pid);

        if ((fd = open(fname, O_RDONLY)) == -1) {
            continue; // iterate over /proc
        }

        int size;
        if ((size = read(fd, buffer, MAX_LEN)) == -1) {
            goto next;
        }

        char* p = buffer;

        while (p <= buffer + size) {
            if (strcmp(magicenv, p) == 0) {
                if (res >= ressize) {
                    ressize += 10;
                    result = realloc(result, ressize * sizeof (pid_t));
                }
                result[res++] = (pid_t) atol(pid);
                break;
            }
            p += strlen(p) + 1;
        }
next:
        (void) close(fd);
    }

    if (dirp != NULL) {
        (void) closedir(dirp);
    }

    if (res >= ressize) {
        ressize++;
        result = realloc(result, ressize * sizeof (pid_t));
    }
    result[res] = 0;

    return result;

}