#ifndef EXITCODES_H
#define	EXITCODES_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#define FAILURE_LOCKING_MUTEX                   201
#define FAILURE_UNLOCKING_MUTEX                 202
#define WRONG_ARGUMENT                          203
#define FAILURE_GETTING_HOME_DIR                204
#define FAILURE_CREATING_STORAGE_SUPER_DIR      205
#define FAILURE_ACCESSING_STORAGE_SUPER_DIR     206
#define FAILURE_CREATING_STORAGE_DIR            207
#define FAILURE_ACCESSING_STORAGE_DIR           208
#define FAILURE_CREATING_TEMP_DIR               209
#define FAILURE_ACCESSING_TEMP_DIR              210
#define FAILURE_CREATING_CACHE_DIR              211
#define FAILURE_ACCESSING_CACHE_DIR             212
#define NO_MEMORY_EXPANDING_DIRTAB              213
#define FAILED_CHDIR                            214
#define FAILURE_OPENING_LOCK_FILE               215
#define FAILURE_LOCKING_LOCK_FILE               216
#define FAILURE_DIRTAB_DOUBLE_CACHE_OPEN        217

#endif	/* EXITCODES_H */

