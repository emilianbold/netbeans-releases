
#include "fs_common.h"
#include "util.h"
#include "array.h"
#include <stdlib.h>
#include <assert.h>

static const int element_size = sizeof(void*);

void array_init(array *a, int capacity) {
    assert(capacity > 0);
    a->capacity = capacity;
    a->size = 0;
    a->data = malloc(capacity * element_size);
    // TODO: error processing (if malloc returns null)
}

void array_ensure_capcity(array *a, int capacity) {
    if (a->capacity < capacity) {
        while (a->capacity < capacity) {
            a->capacity *= 2;
        }
        a->data = realloc(a->data, a->capacity * element_size);
    }
}

void array_truncate(array *a) {
    if (a->size != a->capacity) {
        a->data = realloc(a->data, a->capacity * element_size);
    }
}

void array_add(array *a, void* element) {
    array_ensure_capcity(a, a->size + 1);
    a->data[a->size++] = element;
}

void* array_get(array *a, int index) {
    assert(index >= 0);
    assert(index < a->size);
    return a->data[index];
}

int array_size(array *a) {
    return a->size;
}

const void *array_iterate(array *a, const void* (*iterator)(const void *element, void* arg), void *arg) {
    for (int i = 0; i < a->size; i++) {
        void* p = a->data[i];
        const void* res = iterator(p, arg);
        if (res) {
            return res;
        }
    }
    return NULL;
}

void array_qsort(array *a, int (*comparator)(const void *element1, const void *element2)) {
    qsort(a->data, a->size, element_size, comparator);
}

void array_free(array *a) {
    if (a) {
        for (int i = 0; i < a->size; i++) {
            free(a->data[i]);
        }
        free(a->data);
        a->size = 0;
        a->data = NULL;
    }
}
