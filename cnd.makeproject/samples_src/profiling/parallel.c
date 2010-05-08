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
#include "parallel.h"

static void estimate_usage(int work_count, work_t* works, int seconds) {
    int usrcpu = usrcpu_usage(work_count, works);
    int syscpu = syscpu_usage(work_count, works);
    int total = usrcpu + syscpu;
    REF("Predicted resource usage for the next %d seconds\n", seconds);
    REF("CPU usage\n");
    REF("\tuser:   about %d%%\n", 100 * usrcpu / MAX(total, cpucount()));
    REF("\tsystem: about %d%%\n", 100 * syscpu / MAX(total, cpucount()));
    REF("Memory usage\n");
    REF("\t%ld bytes\n", work_count * sizeof (pthread_t) + mem_usage(work_count, works));
    REF("Thread usage\n");
    REF("\ttotal:  %d\n", work_count + 1);
    REF("\tlocked: %d\n", 0);
}

static volatile int done;
static pthread_barrier_t start;

static void* parallel_threadfunc(void *p) {
    pthread_barrier_wait(&start);
    work_t* work = (work_t*) p;
    while (!done) {
        work_run(work, MICROS_PER_SECOND);
    }
    return NULL;
}

void parallel_demo(int work_count, work_t* works, int seconds_per_work) {
    PRINT("*** PARALLEL DEMO ***\n\n");
    EXPLAIN("  I'm going to run %d works in parallel, each work in its own thread.\n", work_count);
    EXPLAIN("  Threads will be created with pthread_create().\n");
    EXPLAIN("  Works will run for %d seconds.\n\n", seconds_per_work);

    int i;
    for (i = 0; i < work_count; ++i) {
        work_explain(&works[i]);
    }
    PAUSE("Press [Enter] to start...\n");

    estimate_usage(work_count, works, seconds_per_work);

    TRACE("Allocating %ld bytes of memory for thread descriptors with calloc()\n", (long) (work_count * sizeof(pthread_t)));
    pthread_t* t = calloc(work_count, sizeof (pthread_t));
    done = 0;

    pthread_barrier_init(&start, NULL, work_count + 1);
    EXPLAIN("Creating threads with pthread_create()\n");
    for (i = 0; i < work_count; ++i) {
        pthread_create(&t[i], NULL, &parallel_threadfunc, &works[i]);
    }

    pthread_barrier_wait(&start);
    usleep(seconds_per_work * MICROS_PER_SECOND);
    done = 1;

    EXPLAIN("Waiting for threads to finish with pthread_join()\n");
    for (i = 0; i < work_count; ++i) {
        pthread_join(t[i], NULL);
    }

    TRACE("Freeing %ld bytes of memory used for thread descriptors\n", (long) (work_count * sizeof(pthread_t)));
    free(t);
    PRINT("\n");
}
