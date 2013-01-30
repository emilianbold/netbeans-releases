/* 
 * File:   util.h
 * Author: akrasny
 *
 * Created on 26 Сентябрь 2012 г., 13:18
 */

#ifndef UTIL_H
#define	UTIL_H

#include <sys/types.h>
#include <unistd.h>

#ifdef	__cplusplus
extern "C" {
#endif

    ssize_t writen(int fd, const void *ptr, size_t n);

#if defined (__CYGWIN__) || defined (WINDOWS)
        extern char *strsignal(int);
#endif    

#ifdef	__cplusplus
}
#endif

#endif	/* UTIL_H */

