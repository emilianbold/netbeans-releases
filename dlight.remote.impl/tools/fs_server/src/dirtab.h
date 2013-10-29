#ifndef DIRTABLE_H
#define	DIRTABLE_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif
 
#include "util.h"

#include <pthread.h>

#ifdef	__cplusplus
extern "C" {
#endif

/**
 * dirtab_* maintains a list of all known directories
 */    

/** initializes dirtab;must be called before any other dirtab_* function */    
void dirtab_init();

/** stores dirtab to file */
bool dirtab_flush();

/** to be called only after init */
const char* dirtab_get_basedir(); 

/** to be called only after init */
const char* dirtab_get_tempdir(); 

/** 
 * Returns a relative path of cache for the given path.
 * If there is no such a file, it is added.
 * The string is allocated in internal dirtab data
 */
const char *dirtab_get_cache(const char* path);

void  dirtab_lock_cache_mutex();

void  dirtab_unlock_cache_mutex();

void dirtab_visit(bool (*visitor) (const char* path, int index, const char* cache));

bool dirtab_is_empty();

#ifdef	__cplusplus
}
#endif

#endif	/* DIRTABLE_H */

