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

    struct options {
        int noecho;
        int nopty;
        int set_erase_key;
        int waitSignal;
        char *pty;
        char *wdir;
        const char *envfile;
        const char *reportfile;
        int envnum;
        char **envvars;
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

