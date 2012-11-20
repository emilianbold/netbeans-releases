/* 
 * File:   pty.h
 * Author: ak119685
 *
 * Created on 22 Апрель 2010 г., 12:33
 */

#ifndef _PTY_H
#define	_PTY_H


#ifdef	__cplusplus
extern "C" {
#endif

#include <sys/types.h>
#include <stdio.h>

    pid_t pty_fork(int *ptrfdm);

    pid_t pty_fork1(const char *pts_name);

#ifdef	__cplusplus
}
#endif

#endif	/* _PTY_H */

