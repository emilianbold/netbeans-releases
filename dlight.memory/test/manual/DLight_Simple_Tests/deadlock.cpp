#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>

#include "worker.h"
#include "mutex.h"

//Mutex barrier("barrier");
Mutex lock1("lock1");
Mutex lock2("lock2");

class DeadlockWorker : public Worker {

protected:

    virtual int run() {
//        barrier.lock();
        printf("%s: running... \n", getName());
//        barrier.unlock();
        if (getId() % 2 == 0) {
            lock1.lock(getName());
            sleep(1);
            lock2.lock(getName());
            lock2.unlock(getName());
            lock1.unlock(getName());
        } else {
            lock2.lock(getName());
            sleep(2);
            lock1.lock(getName());
            lock1.unlock(getName());
            lock2.unlock(getName());
        }
        return 0;
    }
public:

    DeadlockWorker() {
        char buf[40];
        sprintf(buf, "DeadlockWorker %d", getId());
        setName(buf);
    }
};

int main_deadlock() {
    DeadlockWorker w1, w2;
//    barrier.lock();
    w1.start();
    //sleep(2);
    w2.start();
//    barrier.unlock();
    w1.join();
    w2.join();
    printf("Done\n");
}

