#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <pthread.h>

#include "worker.h"

int main_worker() {
    Worker w1("One"), w2("two");
    const int cnt = 3;
    Worker workers[cnt];

    w1.start();
    w2.start();

    for (int i = 0; i < cnt; i++) {
        workers[i].start();
    }

    w1.join();
    w2.join();
    for (int i = 0; i < cnt; i++) {
        workers[i].join();
        printf("%s returned %d\n", workers[i].getName(), workers[i].getRetCode());
    }

    printf("%s returned %d\n", w1.getName(), w1.getRetCode());
    printf("%s returned %d\n", w2.getName(), w2.getRetCode());

     return 0;
}
