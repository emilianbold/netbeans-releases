/*
 * Copyright (c) 2009, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include "mutex.h"

static long mem_min(int threads, work_t* works) {
    int i;
    unsigned long min = (unsigned long) -1;
    for (i = 0; i < threads; ++i) {
        if (works[i].mem_size < min) {
            min = works[i].mem_size;
        }
    }
    return min;
}

static long mem_max(int threads, work_t* works) {
    int i;
    unsigned long max = 0;
    for (i = 0; i < threads; ++i) {
        if (max < works[i].mem_size) {
            max = works[i].mem_size;
        }
    }
    return max;
}

static void estimate_usage(int threads, work_t* works, int seconds) {
    int usrcpu = usrcpu_usage(threads, works);
    int usrcpumin = (usrcpu < threads)? 0 : (100 / cpucount());
    int usrcpumax = (0 < usrcpu)? (100 / cpucount()) : 0;
    int syscpu = syscpu_usage(threads, works);
    int syscpumin = (syscpu < threads)? 0 : (100 / cpucount());
    int syscpumax = (0 < syscpu)? (100 / cpucount()) : 0;
    REF("Predicted resource usage for the next %d seconds:\n"
            "CPU usage\n"
            "\tuser:   %3d%% <= x <= %3d%%\n"
            "\tsystem: %3d%% <= x <= %3d%%\n"
            "Memory usage\n"
            "\t%ld bytes <= x <= %ld bytes\n"
            "Thread usage\n"
            "\ttotal:  %d\n"
            "\tlocked: %d\n",
            seconds,
            usrcpumin, usrcpumax,
            syscpumin, syscpumax,
            threads * sizeof (pthread_t) + mem_min(threads, works),
            threads * sizeof (pthread_t) + mem_max(threads, works),
            threads + 1, threads - 1);
}

static volatile int done;
static pthread_barrier_t start;
static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

static void* mutex_threadfunc(void *p) {
    pthread_barrier_wait(&start);
    work_t* work = (work_t*) p;
    while (!done) {
        pthread_mutex_lock(&mutex);
        TRACE("work %d: locked mutex with pthread_mutex_lock()\n", work->id);
        if (!done) {
            work_run(work, MICROS_PER_SECOND);
        }
        TRACE("work %d: releasing mutex with pthread_mutex_unlock()\n", work->id);
        pthread_mutex_unlock(&mutex);
        usleep(MICROS_PER_SECOND / 100);
    }
    return NULL;
}

void mutex_demo(int work_count, work_t* works, int seconds) {
    PRINT("*** PTHREAD MUTEX DEMO ***\n\n");
    EXPLAIN("  I'm going to run %d works in parallel.\n", work_count);
    EXPLAIN("  Each work tries to lock a mutex with pthread_mutex_lock().\n");
    EXPLAIN("  While mutex is locked by one work, others can not lock it again.\n");
    EXPLAIN("  Work releases the mutex in a second with pthread_mutex_unlock().\n\n");

    int i;
    for (i = 0; i < work_count; ++i) {
        work_explain(&works[i]);
    }
    PAUSE("Press [Enter] to start...\n");

    estimate_usage(work_count, works, seconds);

    TRACE("Allocating %ld bytes of memory for thread descriptors with calloc()\n", (long) (work_count * sizeof(pthread_t)));
    pthread_t* t = calloc(work_count, sizeof (pthread_t));
    done = 0;
    pthread_barrier_init(&start, NULL, work_count + 1);

    EXPLAIN("Creating threads with pthread_create()\n");
    for (i = 0; i < work_count; ++i) {
        pthread_create(&t[i], NULL, &mutex_threadfunc, &works[i]);
    }

    pthread_barrier_wait(&start);
    usleep(seconds * MICROS_PER_SECOND);
    done = 1;

    EXPLAIN("Waiting for threads to finish with pthread_join()\n");
    for (i = 0; i < work_count; ++i) {
        pthread_join(t[i], NULL);
    }

    TRACE("Freeing %ld bytes of memory used for thread descriptors\n", (long) (work_count * sizeof(pthread_t)));
    free(t);
    PRINT("\n");
}
