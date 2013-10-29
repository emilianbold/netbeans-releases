
#ifndef UTIL_H
#define	UTIL_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#include <pthread.h>
#include <stdio.h>
#include <dirent.h>

#ifdef	__cplusplus
extern "C" {
#endif

void set_trace(bool on_off);
bool get_trace();

void report_error(const char *format, ...);
void trace(const char *format, ...);
void soft_assert(int condition, char* format, ...);

void mutex_unlock(pthread_mutex_t *mutex);
void mutex_lock(pthread_mutex_t *mutex);

const char* get_home_dir();
bool file_exists(const char* path);

int fclose_if_not_null(FILE* f);
int closedir_if_not_null(DIR *d);

void stopwatch_start();
void stopwatch_stop(const char* message);

char *replace_first(char *s, char c, char replacement);

#ifdef	__cplusplus
}
#endif

#endif	/* UTIL_H */

