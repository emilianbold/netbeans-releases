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
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "common.h"

#ifdef HAVE_PTHREAD_SPINLOCK

static long mem_min(int threads, work_t* works) {
    int i;
    unsigned long min = (unsigned long) -1;
    for (i = 0; i < threads; ++i) {
        if (works[i].mem_usage && works[i].mem_size < min) {
            min = works[i].mem_size;
        }
    }
    return min;
}

static long mem_max(int threads, work_t* works) {
    int i;
    unsigned long max = 0;
    for (i = 0; i < threads; ++i) {
        if (works[i].mem_usage && max < works[i].mem_size) {
            max = works[i].mem_size;
        }
    }
    return max;
}

static void spinlock_demo_header(int threads, work_t* works) {
    char minbuf[64], maxbuf[64];
    int usrcpu = usrcpu_usage(threads, works);
    int usrcpumin = (usrcpu < threads)? 0 : (100 / cpucount());
    int usrcpumax = (0 < usrcpu)? (100 / cpucount()) : 0;
    int syscpu = syscpu_usage(threads, works);
    int syscpumin = (syscpu < threads)? 0 : (100 / cpucount());
    int syscpumax = (0 < syscpu)? (100 / cpucount()) : 0;
    mem2str(minbuf, threads * sizeof (pthread_t) + mem_min(threads, works));
    mem2str(maxbuf, threads * sizeof (pthread_t) + mem_max(threads, works));
    REF("*** PTHREAD SPINLOCK DEMO ***\n"
            "CPU usage\n"
            "\tuser:   %3d%% <= x <= %3d%%\n"
            "\tsystem: %3d%% <= x <= %3d%%\n"
            "Memory usage\n"
            "\t%s <= x <= %s\n"
            "Thread usage\n"
            "\ttotal:  %d\n"
            "\tlocked: %d\n",
            usrcpumin, usrcpumax,
            syscpumin, syscpumax,
            minbuf, maxbuf,
            threads + 1, threads - 1);
}

static volatile int done;
static pthread_barrier_t start;
static pthread_spinlock_t spinlock;

static void* spinlock_threadfunc(void *p) {
    pthread_barrier_wait(&start);
    work_t* work = (work_t*) p;
    while (!done) {
        pthread_spin_lock(&spinlock);
        if (!done) {
            work_run(work, MICROS_PER_SECOND);
        }
        pthread_spin_unlock(&spinlock);
        usleep(MICROS_PER_SECOND / 100);
    }
    return NULL;
}

void spinlock_demo(int threads, work_t* works, int seconds) {
    int i;
    pthread_t *t;
    spinlock_demo_header(threads, works);
    t = calloc(threads, sizeof (pthread_t));
    done = 0;
    pthread_spin_init(&spinlock, PTHREAD_PROCESS_PRIVATE);
    pthread_barrier_init(&start, NULL, threads + 1);
    for (i = 0; i < threads; ++i) {
        pthread_create(&t[i], NULL, &spinlock_threadfunc, &works[i]);
    }
    pthread_barrier_wait(&start);
    usleep(seconds * MICROS_PER_SECOND);
    done = 1;
    EXPLAIN("Stopping worker threads...\n");
    for (i = 0; i < threads; ++i) {
        EXPLAIN("\twaiting for thread %d to finish\n", i + 1);
        pthread_join(t[i], NULL);
    }
    free(t);
}

#else

void spinlock_demo(int threads, work_t* works, int seconds) {
    EXPLAIN("Spinlocks are not supported on this platform\n");
    PAUSE("Press [Enter] to skip...\n");
}

#endif
