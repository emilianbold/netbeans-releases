/* 
 * File:   array.h
 * Author: vkvashin
 *
 * Created on October 28, 2013, 4:35 PM
 */

#ifndef ARRAY_H
#define	ARRAY_H

#ifdef	__cplusplus
extern "C" {
#endif

typedef struct {
    int size;
    int capacity;
    void** data;
} array;

void array_init(array *a, int capacity);

void array_ensure_capcity(array *a, int capacity);

void array_truncate(array *a);

void array_add(array *a, void* element);

void* array_get(array *a, int index);

int array_size(array *a);

const void *array_iterate(array *a, const void* (*iterator)(const void *element, void* arg), void *arg);

void array_qsort(array *a, int (*comparator)(const void *element1, const void *element2));

void array_free(array *a);

#ifdef	__cplusplus
}
#endif

#endif	/* ARRAY_H */

