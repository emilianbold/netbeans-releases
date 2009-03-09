/* 
 * File:   instruments.h
 * Author: ll155635
 *
 * Created on February 12, 2009, 1:09 PM
 */

#ifndef _INSTRUMENTS_H
#define	_INSTRUMENTS_H

enum {
    MEMMSG  = 1,
    SYNCMSG = 2,
    CPUMSG  = 3,
    FAILMSG = 66,
    CTRLMSG = 99
};

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

typedef struct cpumsg {
    long    type;
    float   user;
    float   sys;
} cpumsg;

typedef struct ctrlmsg {
    long    type;
    int     control;
    int     action;
} ctrlmsg;

typedef struct failmsg {
    long    type;
    int     control;
} failmsg;


#define DEF_RES (10000)
#define GRANULARITY (5)

#endif	/* _INSTRUMENTS_H */

