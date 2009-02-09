/* 
 * File:   test_sync_trivial.h
 * Author: vk155633
 *
 * Created on December 12, 2008, 5:55 PM
 */

#ifndef _TEST_SYNC_TRIVIAL_H
#define	_TEST_SYNC_TRIVIAL_H

#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#ifdef	__cplusplus
extern "C" {
#endif

static pthread_mutex_t trivial_mutex;

static void* test_sync_trivial_thread_start(void *ptr) {
    printf("test_sync_trivial_thread_start: locking...\n");
    pthread_mutex_lock(&trivial_mutex);
    printf("test_sync_trivial_thread_start: locked.\n");
    pthread_mutex_unlock(&trivial_mutex);
    printf("test_sync_trivial_thread_start: unlocked.\n");
}

static void* sleeping_thread_start(void *ptr) {
    printf("sleeping_thread_start: sleeping...\n");
    sleep(10);
    printf("sleeping_thread_start: awoke...\n");
}

void test_sync_trivial_run(int step) {
    pthread_mutex_init(&trivial_mutex, NULL);
    pthread_mutex_lock(&trivial_mutex);
    pthread_t thread;
    pthread_create(&thread, NULL, test_sync_trivial_thread_start, NULL);
    printf("test_sync_trivial: sleeping 10 sec\n");
    sleep(10);
    {
        pthread_t thread1;
        pthread_create(&thread1, NULL, sleeping_thread_start, NULL);
        pthread_t thread2;
        pthread_create(&thread2, NULL, sleeping_thread_start, NULL);
    }
    printf("test_sync_trivial: sleeping 10 sec more\n");
    sleep(10);
    pthread_mutex_unlock(&trivial_mutex);
}

#ifdef	__cplusplus
}
#endif

#endif	/* _TEST_SYNC_TRIVIAL_H */

