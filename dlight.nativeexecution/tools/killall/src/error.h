/* 
 * File:   err.h
 * Author: ak119685
 *
 * Created on 23 Апрель 2010 г., 0:25
 */

#ifndef _ERROR_H
#define	_ERROR_H


#ifdef	__cplusplus
extern "C" {
#endif

#define MAXLINE 4096 /* max line length */

#include <errno.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

    void err_sys(const char *fmt, ...);
    void warn_sys(const char *fmt, ...);
    void err_quit(const char *fmt, ...);

#ifdef	__cplusplus
}
#endif

#endif	/* _ERROR_H */

