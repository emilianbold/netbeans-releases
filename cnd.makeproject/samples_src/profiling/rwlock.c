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
#include "rwlock.h"

#ifdef HAVE_PTHREAD_RWLOCK

static void rwlock_demo_header(int readers, int writers, work_t* works) {
    char buf[64];
    int usrcpu = usrcpu_usage(readers, works);
    int syscpu = syscpu_usage(readers, works);
    int total = usrcpu + syscpu;
    usrcpu = 100 * usrcpu / MAX(total, cpucount());
    syscpu = 100 * syscpu / MAX(total, cpucount());
    mem2str(buf, (readers + writers) * sizeof (pthread_t) + mem_usage(readers, works));
    TRACE("*** PTHREAD RWLOCK DEMO ***\n"
            "CPU usage\n"
            "\tuser:   about %3d%%\n"
            "\tsystem: about %3d%%\n"
            "Memory usage\n"
            "\t%s\n"
            "Thread usage\n"
            "\ttotal:  %d\n"
            "\tlocked: %d\n",
            usrcpu, syscpu,
            buf, readers + writers + 1, writers);
}

static volatile int done;
static pthread_barrier_t start;
static pthread_rwlock_t rwlock = PTHREAD_RWLOCK_INITIALIZER;

static void* readerfunc(void *p) {
    pthread_barrier_wait(&start);
    work_t* work = (work_t*) p;
    while (!done) {
        pthread_rwlock_rdlock(&rwlock);
        if (!done) {
            work_run(work, MICROS_PER_SECOND);
        }
        pthread_rwlock_unlock(&rwlock);
    }
    return NULL;
}

static void* writerfunc(void *p) {
    pthread_barrier_wait(&start);
    work_t* work = (work_t*) p;
    while (!done) {
        pthread_rwlock_wrlock(&rwlock);
        if (!done) {
            work_run(work, MICROS_PER_SECOND / 100);
        }
        pthread_rwlock_unlock(&rwlock);
        usleep(MICROS_PER_SECOND / 10);
    }
    return NULL;
}

void rwlock_demo(int readers, int writers, work_t* works, int seconds) {
    long i;
    pthread_t *t;
    rwlock_demo_header(readers, writers, works);
    t = calloc(readers + writers, sizeof (pthread_t));
    done = 0;
    pthread_barrier_init(&start, NULL, readers + writers + 1);
    for (i = 0; i < readers; ++i) {
        pthread_create(&t[i], NULL, &readerfunc, &works[i]);
    }
    for (i = readers; i < readers + writers; ++i) {
        pthread_create(&t[i], NULL, &writerfunc, &works[i]);
    }
    pthread_barrier_wait(&start);
    usleep(seconds * MICROS_PER_SECOND);
    done = 1;
    printf("Cleaning up...\n\n");
    for (i = 0; i < readers + writers; ++i) {
        pthread_join(t[i], NULL);
    }
    free(t);
}

#else

void rwlock_demo(int readers, int writers, work_t* works, int seconds) {
    EXPLAIN("RW-locks are not supported on this platform\n");
    PAUSE("Press [Enter] to skip...\n");
}

#endif
