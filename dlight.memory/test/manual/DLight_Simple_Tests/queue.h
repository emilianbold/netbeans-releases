#ifndef _QUEUE_H
#define	_QUEUE_H

#include "mutex.h"

template <class T> class Queue {

    struct Node;
    struct Node {
        Node(T data, Node* next) {
            this->data = data;
            this->next = next;
        }
        T data;
        Node* next;
    };

    Node* head;
    Node* tail;

    pthread_cond_t cond;
    pthread_mutex_t mutex;


public:

    Queue() : head(NULL), tail(NULL) {
        pthread_cond_init(&cond, NULL);
        pthread_mutex_init(&mutex, NULL);
    }
    
    void add(T value) {
        Node *pNode = new Node(value, NULL);
        pthread_mutex_lock(&mutex);
        if (tail) {
            tail->next = pNode;
            tail = pNode;
        } else {
            head = tail = pNode;
        }
        pthread_cond_signal(&cond);
        pthread_mutex_unlock(&mutex);
    }

    T poll() {
        printf("\t>\n");
        pthread_mutex_lock(&mutex);
        printf("\t<\n");
        while (!head) {
            printf("queue is empty - waiting on lock...\n");
            pthread_cond_wait(&cond, &mutex);
            //printf("stopped waiting on lock; data %s\n", head ? head->data, NULL);
        }
        T result = head->data;
        head = head->next;
        if (!head) {
            tail = NULL;
        }
        pthread_mutex_unlock(&mutex);
        return result;
    }

    bool empty() {
        //pthread_mutex_lock(&mutex);
        bool result = (head == NULL);
        //pthread_mutex_unlock(&mutex);
        return result;
    }
};


#endif	/* _QUEUE_H */

