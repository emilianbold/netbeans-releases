
#include "queue.h"

#include <unistd.h>

#include <stdio.h>
#include <stdlib.h>

#include <pthread.h>

//#include "mutex.h"
#include "worker.h"
#include "queue.h"

#include "test_sync.h"

pthread_cond_t glob_cond;
pthread_mutex_t trivial_mutex;

class QueueProcessor : public Worker {

    Queue<const char*> queue;
    //bool proceed;
    
public:

    QueueProcessor(const char* name, Queue<const char*> _queue) :
        Worker(name),
        //proceed(true),
        queue(_queue) {}
    
protected:

    virtual int run() {
        printf("starting queue processor %s\n", getName());
        const char* data = NULL;
        do {
            data = queue.poll();
            printf("%s: processing %s\n", getName(), data);
            //sleep(1);
            printf("%s: done with %s\n", getName(), data);
        } while (data != NULL);
        return 0;
    }
};

class SyncWorker : public Worker {
    //Mutex& mutex;
public:
    SyncWorker(const char* name/*, Mutex& _mutex*/) : Worker(name)/*, mutex(_mutex)*/ {
    }
protected:
    virtual int run() {
        printf("%s waiting on lock\n", getName());
        //mutex.lock();
        pthread_mutex_lock(&trivial_mutex);
        printf("%s locked\n", getName());
        //mutex.unlock();
        pthread_mutex_unlock(&trivial_mutex);
        printf("%s done\n", getName());
        return 0;
    }

};

static Queue<const char*> queue;
static QueueProcessor qp1("QP_1", queue);
static SyncWorker sw("SW");

void testWait() {

    queue.add("111a"); printf("added 111a\n");
    queue.add("222a"); printf("added 222a\n");
    queue.add("333a"); printf("added 333a\n");

    QueueProcessor qp1("QP_1", queue);
    qp1.start();

    sleep(4);
    queue.add("qwe1"); printf("added qwe1\n");
    queue.add("asd1"); printf("added asd1\n");
    queue.add("zxc1"); printf("added zxc1\n");
    //testQueue();
    sleep(1);
    queue.add("ZZZ");printf("added ZZZ\n");
    queue.add(NULL);
    printf("locking glob_mutex\n");


    pthread_mutex_lock(&trivial_mutex);

    SyncWorker sw("SW");
    sw.start();

    sleep(10);
    pthread_mutex_unlock(&trivial_mutex);

    printf("done\n\n");

    int cnt = 1000000;
    for(int i = 0; i < cnt; i++) {
        char buffer[256];
        sprintf(buffer, "line %d", i);
        queue.add(buffer); printf("added %s\n", buffer);
        sleep(1);
    }

    printf("joining...");

    pthread_mutex_unlock(&trivial_mutex);
    
//    sw.join();
//    qp1.join();
    printf("Exiting main\n");
}

extern "C" void test_queue() {
    Queue<const char*> queue;
    queue.add("1");
//    const char* p = queue.poll();
//    printf("%s\n", p);
    queue.add("2");
    queue.add("3");
    queue.add(NULL);
    printf("cycling\n");
    while (!queue.empty()) {
        const char* p = queue.poll();
        printf("%s\n", p);
    }
}

void testLockUnlock() {
    pthread_cond_t cond;
    pthread_mutex_t mutex;
    pthread_cond_init(&cond, NULL);
    pthread_mutex_init(&mutex, NULL);
    pthread_cond_signal(&cond);
    pthread_mutex_lock(&mutex);
    pthread_mutex_unlock(&mutex);
}

extern "C" void test_sync_init() {

    pthread_cond_init(&glob_cond, NULL);
    pthread_mutex_init(&trivial_mutex, NULL);

    queue.add("111a"); printf("added 111a\n");
    queue.add("222a"); printf("added 222a\n");
    queue.add("333a"); printf("added 333a\n");
    
    qp1.start();

    printf("locking glob_mutex\n");
    pthread_mutex_lock(&trivial_mutex);

    sw.start();
}

extern "C" void test_sync_step(int step) {
    char buffer[256];
    sprintf(buffer, "line %d", step);
    queue.add(buffer); printf("added %s\n", buffer);
}

extern "C" void test_wait_shutdown() {
    printf("unlocking glob_mutex\n");
    pthread_mutex_unlock(&trivial_mutex);
//    printf("joining...");
//    sw.join();
//    qp1.join();
    printf("exiting");
}

int main_wait(int argc, char** argv) {
    pthread_cond_init(&glob_cond, NULL);
    pthread_mutex_init(&trivial_mutex, NULL);
    //testLockUnlock();
    //testQueue();
    testWait();
    return 0;
}
