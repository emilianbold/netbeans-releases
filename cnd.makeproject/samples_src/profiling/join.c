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
#include "join.h"

static void join_demo_header(int threads, work_t* works) {
    char buf[64];
    int usrcpu = usrcpu_usage(threads, works);
    int syscpu = syscpu_usage(threads, works);
    mem2str(buf, mem_usage(threads, works) + sizeof(pthread_t) * threads);
    TRACE("*** PTHREAD JOIN DEMO ***\n"
            "CPU usage\n"
            "\tuser:   drops from %3d%% to 0%%\n"
            "\tsystem: drops from %3d%% to 0%%\n"
            "Memory usage\n"
            "\tdrops from %s to 0\n"
            "Thread usage\n"
            "\ttotal:  drops from %d to 0\n"
            "\tlocked: none\n",
            100 * usrcpu / MAX(usrcpu + syscpu, cpucount()),
            100 * syscpu / MAX(usrcpu + syscpu, cpucount()),
            buf, threads + 1);
}

static pthread_barrier_t start;

static void* join_threadfunc(void *p) {
    pthread_barrier_wait(&start);
    work_t* work = (work_t*) p;
    work_run(work, 5 * (1 + work->id) * MICROS_PER_SECOND);
    return NULL;
}

void join_demo(int threads, work_t* works) {
    long i;
    pthread_t* t;
    join_demo_header(threads, works);
    t = calloc(threads, sizeof (pthread_t));
    pthread_barrier_init(&start, NULL, threads + 1);
    for (i = 0; i < threads; ++i) {
        pthread_create(&t[i], NULL, &join_threadfunc, &works[i]);
    }
    pthread_barrier_wait(&start);
    for (i = 0; i < threads; ++i) {
        pthread_join(t[i], NULL);
    }
    printf("Cleaning up...\n\n");
}
