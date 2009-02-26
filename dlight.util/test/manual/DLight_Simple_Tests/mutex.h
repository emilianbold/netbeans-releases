#ifndef _MUTEX_H
#define	_MUTEX_H

#include <pthread.h>
#include <stdio.h>
#include <string.h>

class Mutex {

    pthread_mutex_t mutex;
    const char* name;

public:

    Mutex() : name("unnamed") {
    }

    Mutex(const char* _name) : name(strdup(_name)) {
        pthread_mutex_init(&mutex, NULL);
    }

    void lock(const char* who) {
        printf("%s waiting on %s\n", who, name);
        pthread_mutex_lock(&mutex);
        printf("%s locked %s\n", who, name);
    }

    void lock() {
        pthread_mutex_lock(&mutex);
    }

    void unlock(const char* who) {
        printf("%s unlocking %s\n", who, name);
        pthread_mutex_unlock(&mutex);
        printf("%s unlocked %s\n", who, name);
    }

    void unlock() {
        pthread_mutex_unlock(&mutex);
    }

//    void wait() {
//        printf("waiting on %s\n", name);
//        pthread_mutex_lock(&mutex);
//        pthread_mutex_unlock(&mutex);
//        printf("finished waiting on %s\n", name);
//    }
};

#endif	/* _MUTEX_H */

