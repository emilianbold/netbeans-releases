/* 
 * File:   dlight.h
 * Author: vk155633
 */

#ifndef _DLIGHT_H
#define	_DLIGHT_H

#ifdef	__cplusplus
extern "C" {
#endif

/** I prefer to prefix types, otherwise once, on some system
 * you get into a symbol redefinition compiler error */
enum dlight_msg_type {
    DLIGHT_MEM = 1,
    DLIGHT_SYNC = 2
};

struct dlight_msg_mem {
    long type;
    long heap_used;
};

#define DLIGHT_ERROR (0x7fffffff)

#ifdef	__cplusplus
}
#endif

#endif	/* _DLIGHT_H */
