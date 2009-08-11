#ifndef _RFS_FILEDATA_H
#define	_RFS_FILEDATA_H

#include <pthread.h>

#ifdef	__cplusplus
extern "C" {
#endif

typedef enum file_state {
    pending = 0,
    ok = 1,
    error = -1
} file_state;

typedef struct file_data {
    volatile file_state state;
    pthread_mutex_t cond_mutex;
    pthread_cond_t cond;
    struct file_data *left;
    struct file_data *right;
    #if TRACE
    int cnt;
    #endif
    char filename[];
} file_data;

/**
 * Finds file_data for the given file name;
 * if it does not exist, creates one, inserts it into the tree and
 * returns a reference to the newly inserted one
 */
file_data *find_file_data(const char* filename);

/**
 * Visits all file_data elements - calls function passed as a 1-st parameter
 * for each file_data element.
 * Two parameters are passed to the function on each call:
 * 1) current file_data
 * 2) pointer that is passed as 2-nd visit_file_data parameter
 * In the case function returns 0, the tree traversal is stopped
 */
void visit_file_data(int (*) (file_data*, void*), void*);

void wait_on_file_data(file_data *fd);
void signal_on_file_data(file_data *fd);

#ifdef	__cplusplus
}
#endif

#endif	/* _RFS_FILEDATA_H */

