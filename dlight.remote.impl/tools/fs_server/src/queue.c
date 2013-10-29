
#include "fs_common.h"
#include "queue.h"

#include <stddef.h>
#include <stdlib.h>
#include <assert.h>

void queue_init(queue *q) {
    q->head = 0;
    q->tail = 0;
}

int  queue_size(queue *q) {
    int size= 0;
    for(queue_node *curr = q->head; curr; curr = curr->next) {
        size++;
    }
    return size;
}

void queue_add(queue *q, void* data) {
    queue_node *n = (queue_node*) malloc(sizeof(queue_node));
    n->data = data;
    n->next = 0;
    if (q->tail){
        assert(q->head);
        assert(!q->tail->next);
        q->tail->next = n;
    } else {
        assert(!q->head);
        q->head = n;        
    }
    q->tail = n;
}

void* queue_poll(queue *q) {
    queue_node* n = 0;
    if (q->head) {
        n = q->head;
        if (q->head->next) {
            q->head = q->head->next;
        } else {
            assert(q->tail == q->head);
            q->head = 0;
            q->tail = 0;
        }
    }
    if (n) {
        void* result = n->data;
        free(n);
        return result;
    } else {
        return NULL;
    }
}

