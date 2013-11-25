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

typedef enum {
    DE_STATE_INITIAL = 0,
    DE_STATE_LS_SENT = 1,
    DE_STATE_REFRESH_SENT = 2
} dirtab_state;

typedef enum {
    /** not watched */
    DE_WSTATE_NONE = 0,
    /** polled periodically */        
    DE_WSTATE_POLL = 1,
    /** watched natively */        
    DE_WSTATE_WATCH = 2
} dirtab_watch_state;

struct dirtab_element;
typedef struct dirtab_element dirtab_element;
    
void dirtab_set_persistence_dir(const char* dir);

/** initializes dirtab;must be called before any other dirtab_* function */    
void dirtab_init(bool clear_persistence, dirtab_watch_state default_watch_state);

/** stores dirtab to file */
bool dirtab_flush();

/** to be called only after init */
const char* dirtab_get_basedir(); 

/** to be called only after init */
const char* dirtab_get_tempdir(); 

dirtab_element *dirtab_get_element(const char* abspath);

void dirtab_lock(dirtab_element *el);

void dirtab_unlock(dirtab_element *el);

const char*  dirtab_get_element_cache_path(dirtab_element *e);

void dirtab_visit(bool (*visitor) (const char* path, int index, dirtab_element* el));

bool dirtab_is_empty();

/** frees all resources*/    
void dirtab_free();

/** call dirtab_lock() before!  */
dirtab_state dirtab_get_state(dirtab_element *el);

/** call dirtab_lock() before!  */
void dirtab_set_state(dirtab_element *el, dirtab_state state);

/** call dirtab_lock() before!  */
dirtab_watch_state dirtab_get_watch_state(dirtab_element *el);

/** call dirtab_lock() before!  */
void dirtab_set_watch_state(dirtab_element *el, dirtab_watch_state state);

#ifdef	__cplusplus
}
#endif

#endif	/* DIRTABLE_H */

