
#include "fs_common.h"
#include "util.h"
#include "blocking_queue.h"

#include <errno.h>

/** Initializes list. A list must be initialized before use */
void blocking_queue_init(blocking_queue *q) {
    queue_init(&q->q);
    pthread_mutex_init(&q->mutex, NULL);
    pthread_cond_init(&q->cond, NULL);
}

/** gets the amunt of elements in the list */
int  blocking_queue_size(blocking_queue *q) {    
    mutex_lock(&q->mutex);
    int size = queue_size(&q->q);
    mutex_unlock(&q->mutex);
    return size;
}

/** adds element to the list tail */
void blocking_queue_add(blocking_queue *q, void* data) {
    mutex_lock(&q->mutex);
    queue_add(&q->q, data);
    pthread_cond_broadcast(&q->cond);
    mutex_unlock(&q->mutex);
}

/** removes and returns element from the list's head */
void* blocking_queue_poll(blocking_queue *q) {
    while (true) {
        mutex_lock(&q->mutex);
        void* result = queue_poll(&q->q);
        if (result) {
            mutex_unlock(&q->mutex);
            return result;
        } else {
            pthread_cond_wait(&q->cond, &q->mutex);
            mutex_unlock(&q->mutex);
        }
    }
}

