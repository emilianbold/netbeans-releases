#ifndef _TEST_ALLOC_H
#define	_TEST_ALLOC_H

#ifdef	__cplusplus
extern "C" {
#endif

void leak_malloc(int size, int step);

void leak_calloc(int size, int step);

void leak_realloc(int size, int step);

#ifdef	__cplusplus
}
#endif

#endif	/* _TEST_ALLOC_H */

