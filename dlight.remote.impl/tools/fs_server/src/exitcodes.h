#ifndef EXITCODES_H
#define	EXITCODES_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#define FAILURE_LOCKING_MUTEX                   1001
#define FAILURE_UNLOCKING_MUTEX                 1002
#define WRONG_ARGUMENT                          1003
#define FAILURE_GETTING_HOME_DIR                1004
#define FAILURE_CREATING_STORAGE_SUPER_DIR      1005
#define FAILURE_ACCESSING_STORAGE_SUPER_DIR     1006
#define FAILURE_CREATING_STORAGE_DIR            1007
#define FAILURE_ACCESSING_STORAGE_DIR           1008
#define FAILURE_CREATING_TEMP_DIR               1009
#define FAILURE_ACCESSING_TEMP_DIR              1010
#define FAILURE_CREATING_CACHE_DIR              1011
#define FAILURE_ACCESSING_CACHE_DIR             1012
#define NO_MEMORY_EXPANDING_DIRTAB              1013
#define FAILED_CHDIR                            1014
#define FAILURE_OPENING_LOCK_FILE               1015
#define FAILURE_LOCKING_LOCK_FILE               1016
#define FAILURE_DIRTAB_DOUBLE_CACHE_OPEN        1017

#endif	/* EXITCODES_H */

