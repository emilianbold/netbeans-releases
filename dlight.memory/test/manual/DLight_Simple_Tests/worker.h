/* 
 * File:   worker.h
 * Author: Vladimir Kvashin
 *
 * Created on November 20, 2008, 2:21 PM
 */

#ifndef _WORKER_H
#define	_WORKER_H

#include <stdio.h>
#include <string.h>
#include <pthread.h>

class Worker {

    const char* name;
    pthread_t thread;
    int ret;
    static int instanceCount;
    int id;

    static void* _start(void *ptr) {
        Worker* worker = (Worker*) ptr;
        int rc = worker->run();
        return (void*) rc;
    }


protected:

    virtual int run() {
        printf("%s: running... \n", getName());
        return 0;
    }

    void setName(const char* name) {
        this->name = strdup(name);
    }

public:

    Worker() : id(instanceCount++) {
        char buf[40];
        sprintf(buf, "Unnamed %d", id);
        setName(buf);
    }

    Worker(const char* name) : id(instanceCount++) {
        setName(name);
    }

    void start() {
        printf("%s: starting... \n", name);
        ret = pthread_create(&thread, NULL, Worker::_start, (void*) this);
    }

    int join() const {
        pthread_join(thread, NULL);
        return ret;
    }

    int getRetCode() const {
        return ret;
    }

    int getId() {
        return id;
    }

    const char* getName() const {
        return name;
    }
};

#endif	/* _WORKER_H */

