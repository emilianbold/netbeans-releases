/* 
 * File:   instruments.h
 * Author: ll155635
 *
 * Created on February 12, 2009, 1:09 PM
 */

#ifndef _INSTRUMENTS_H
#define	_INSTRUMENTS_H

#define MEMMSG  (1)
#define SYNCMSG (2)

#define FAILURE (0x7fffffff)

typedef struct memmsg {
    long    type;
    long    heapused;
} memmsg;

typedef struct syncmsg {
    long    type;
    long    lock_ticks;
    int     thr_count;
} syncmsg;

#define GRANULARITY (5)

#endif	/* _INSTRUMENTS_H */

