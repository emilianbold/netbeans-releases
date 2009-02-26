#include "pi.h"

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

//#define num_steps 200000000

/*
double pi(int num_steps) {

    double pi = 0;
    int i;

    for (i = 0; i < num_steps; i++) {
        pi += 1.0 / (i * 4.0 + 1.0);
        pi -= 1.0 / (i * 4.0 + 3.0);
    }

    pi = pi * 4.0;
    printf("pi done - %f \n", pi);

    return pi;
}
*/

#define THREADS 4
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
static int num_steps;
double pi = 0;

void *work(void *arg)
{
  int start;
  int end;
  int i;

    start = (num_steps/THREADS) * ((int )arg) ;
    end = start + num_steps/THREADS;

    for (i = start; i < end; i++) {
        pthread_mutex_lock(&mutex);
        pi += 1.0/(i*4.0 + 1.0);
        pi -= 1.0/(i*4.0 + 3.0);
        pthread_mutex_unlock(&mutex);

    }

    return NULL;
}

double calc_pi(int _num_steps) {

    clock_t time_start = clock();

    num_steps = _num_steps;
    int i;
    pthread_t tids[THREADS-1];

    for (i = 0; i < THREADS - 1 ; i++) {
         pthread_create(&tids[i], NULL, work, (void *)i);
    }

    i = THREADS-1;
    work((void *)i);

    for (i = 0; i < THREADS - 1 ; i++) {
        pthread_join(tids[i], NULL);

    }

    pi = pi * 4.0;

    clock_t time_end = clock();
    clock_t time = (time_end - time_start);
    time /= (CLOCKS_PER_SEC/1000);
    printf("pi done - %f  time: %d ms \n", pi, time);

    return pi;
}
