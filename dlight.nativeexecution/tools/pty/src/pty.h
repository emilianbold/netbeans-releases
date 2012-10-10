#ifndef PTY_H
#define	PTY_H

#ifdef	__cplusplus
extern "C" {
#endif
#include "pty_fork.h"
#include "env.h"
#include "loop.h"
#include "error.h"
#include "util.h"
#include "options.h"
#include <sys/termios.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <sys/times.h>
#include <sys/resource.h>
#include <unistd.h>
#include <signal.h>
#include <fcntl.h>
#include <libgen.h>
#include <limits.h>

#if defined __CYGWIN__ && !defined WCONTINUED
    //added for compatibility with cygwin 1.5
#define WCONTINUED 0
#endif

    extern int putenv(char *);

#ifdef	__cplusplus
}
#endif

#endif	/* PTY_H */

