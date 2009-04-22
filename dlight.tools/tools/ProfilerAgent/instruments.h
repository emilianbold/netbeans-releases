/* 
 * File:   instruments.h
 * Author: ll155635
 *
 * Created on February 12, 2009, 1:09 PM
 */

#ifndef _INSTRUMENTS_H
#define	_INSTRUMENTS_H

// Define this to a signed 32-bit integer type.
// In GCC int is 32 bits wide with both -m32 and -m64.
#define I32 int

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
    I32     heapused;
} memmsg;

typedef struct syncmsg {
    long    type;
    I32     lock_ticks;
    I32     thr_count;
} syncmsg;

typedef struct cpumsg {
    long    type;
    I32     user;
    I32     sys;
} cpumsg;

typedef struct ctrlmsg {
    long    type;
    I32     control;
    I32     action;
} ctrlmsg;

typedef struct failmsg {
    long    type;
    I32     control;
} failmsg;


#define DEF_RES (10000)
#define GRANULARITY (5)

#endif	/* _INSTRUMENTS_H */

