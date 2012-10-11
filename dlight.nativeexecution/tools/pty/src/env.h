/* 
 * File:   env.h
 * Author: akrasny
 *
 * Created on 26 Сентябрь 2012 г., 13:13
 */

#ifndef ENV_H
#define	ENV_H

#ifdef	__cplusplus
extern "C" {
#endif

    int dumpenv(const char* fname);
    char** readenv(const char* fname);

#ifdef	__cplusplus
}
#endif

#endif	/* ENV_H */

