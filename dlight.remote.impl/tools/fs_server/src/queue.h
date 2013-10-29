
#ifndef LIST_H
#define	LIST_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#ifdef	__cplusplus
extern "C" {
#endif

typedef struct queue_node {
    void *data;
    struct queue_node *next;
} queue_node;
    
typedef struct queue {
    queue_node* head;
    queue_node* tail;
} queue;

/** Initializes list. A list must be initialized before use */
void queue_init(queue *q);

/** gets the amunt of elements in the list */
int  queue_size(queue *q);

/** adds element to the list tail */
void queue_add(queue *q, void* data);

/** removes and returns element from the list's head */
void* queue_poll(queue *q);

#ifdef	__cplusplus
}
#endif

#endif	/* LIST_H */

