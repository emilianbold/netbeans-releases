#ifndef _CONDITION_H
#define	_CONDITION_H

#include <pthread.h>

class Condition {
    
    pthread_cond_t cond;
    pthread_mutex_t mutex;

public:
    
    Condition() {
        pthread_cond_init(cond, NULL);
        pthread_mutex_init(&mutex, NULL);
    }

    void wait() {
        pthread_mutex_lock(&mutex);
        pthread_cond_wait( &cond, &mutex );
        pthread_mutex_unlock(&mutex);
    }
    

    void signal() {
        pthread_mutex_lock(&mutex);
        pthread_cond_signal(cond);
        pthread_mutex_unlock(&mutex);
    }
};

#endif	/* _CONDITION_H */

