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

struct dirtab_element;
typedef struct dirtab_element dirtab_element;
    
/** initializes dirtab;must be called before any other dirtab_* function */    
void dirtab_init();

/** stores dirtab to file */
bool dirtab_flush();

/** to be called only after init */
const char* dirtab_get_basedir(); 

/** to be called only after init */
const char* dirtab_get_tempdir(); 

dirtab_element *dirtab_get_element(const char* abspath);

FILE* dirtab_get_element_cache(dirtab_element *e, bool writing);

void dirtab_release_element_cache(dirtab_element *e);

//FILE* dirtab_get_cache(const char* abspath, bool writing);

//void dirtab_release_cache(const char* abspath);

void  dirtab_lock_cache_mutex();

void  dirtab_unlock_cache_mutex();

void dirtab_visit(bool (*visitor) (const char* path, int index, dirtab_element* el));

bool dirtab_is_empty();

/** frees all resources*/    
void dirtab_free();

#ifdef	__cplusplus
}
#endif

#endif	/* DIRTABLE_H */

