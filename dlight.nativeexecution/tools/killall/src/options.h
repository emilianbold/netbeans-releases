/* 
 * File:   options.h
 * Author: akrasny
 *
 * Created on 27 Сентябрь 2012 г., 14:44
 */

#ifndef OPTIONS_H
#define	OPTIONS_H

#include <stdio.h>

#ifdef	__cplusplus
extern "C" {
#endif

#define P_QUEUE 1000

    typedef enum {
        S_PID, /* A process identifier */
        S_PGID, /* A process group */
        S_SID /* A session identifier */
    } sigscope_t;

    struct options {
        int id;
        sigscope_t scope;
        int sig;
        const char* magicenv;
    };

    typedef struct options options_t;

    /**
     * 
     * @param opts - return value
     * @return number of parsed options
     */
    int readopts(int argc, char** argv, options_t* opts);

#ifdef	__cplusplus
}
#endif

#endif	/* OPTIONS_H */

