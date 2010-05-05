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

#include <stdlib.h>
#include <sys/mman.h>
#include <sys/time.h>
#include <unistd.h>
#include "common.h"

// workaround for Mac
#ifndef MAP_ANONYMOUS
#define MAP_ANONYMOUS MAP_ANON
#endif

int msg_levels = -1;

static void time_add(struct timeval* time, long micros) {
    micros += time->tv_usec;
    time->tv_sec += micros / MICROS_PER_SECOND;
    time->tv_usec = micros % MICROS_PER_SECOND;
}

static int time_before(struct timeval* a, struct timeval* b) {
    return (a->tv_sec < b->tv_sec) || (a->tv_sec == b->tv_sec && a->tv_usec < b->tv_usec);
}

static void work_run_idle(int work_id, long micros) {
    TRACE("work %d: Sleeping for %ld microseconds with usleep()\n", work_id, micros);
    usleep(micros);
    TRACE("work %d: Done sleeping\n", work_id);
}

static void work_run_usrcpu(int work_id, long micros) {
    TRACE("work %d: Starting mathematical calculations...\n", work_id);
    long i = 0, j = 0;
    double pi = 0;
    struct timeval curtime, endtime;
    gettimeofday(&endtime, 0);
    time_add(&endtime, micros);
    for (;;) {
        gettimeofday(&curtime, 0);
        if (!time_before(&curtime, &endtime)) {
            break;
        }
        for (j = i + 1000; i < j; ++i) {
            pi += 1.0 / (i * 4.0 + 1.0);
            pi -= 1.0 / (i * 4.0 + 3.0);
        }
    }
    TRACE("work %d: Completed calculation, did %ld iterations\n", work_id, j)
}

static void work_run_syscpu(int work_id, long micros) {
    TRACE("work %d: Starting writing to temporary file...\n", work_id);
    FILE* fd;
    long i = 0, j = 0;
    char buf[1024];
    struct timeval curtime, endtime;
    gettimeofday(&endtime, 0);
    time_add(&endtime, micros);
    if ((fd = tmpfile())) {
        for (;;) {
            gettimeofday(&curtime, 0);
            if (!time_before(&curtime, &endtime)) {
                break;
            }
            for (j = i + 10; i < j; ++i) {
                fwrite(buf, 1, sizeof (buf), fd);
                fflush(fd);
            }
        }
        fclose(fd);
    }
    TRACE("work %d: Completed writing to file, wrote %ld bytes\n", work_id, j * sizeof(buf));
}

static void* work_run_getmem(work_t* work) {
    void* ptr = NULL;
    switch (work->mem_usage) {
        case mem_none:
            // relax
            break;
        case mem_malloc:
            TRACE("work %d: Allocating %ld bytes of memory with malloc()... ", work->id, work->mem_size);
            ptr = malloc(work->mem_size);
            if (ptr) {
                TRACE("OK\n");
            } else {
                TRACE("failed\n");
            }
            break;
        case mem_mmap:
            TRACE("work %d: Allocating %ld bytes of memory with mmap()... ", work->id, work->mem_size);
            ptr = mmap(NULL, work->mem_size, PROT_READ, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
            if (ptr == MAP_FAILED) {
                ptr = NULL;
                TRACE("failed\n");
            } else {
                TRACE("OK\n");
            }
            break;
        default:
            ERROR("Unknown memory usage type: %d\n", work->mem_usage);
    }
    return ptr;
}

static void work_run_freemem(work_t* work, void* ptr) {
    if (!ptr) {
        return;
    }
    switch (work->mem_usage) {
        case mem_none:
            // relax
            break;
        case mem_malloc:
            TRACE("work %d: Freeing memory with free()\n", work->id);
            free(ptr);
            break;
        case mem_mmap:
            TRACE("work %d: Freeing memory with munmap()\n", work->id);
            munmap(ptr, work->mem_size);
            break;
        default:
            ERROR("Unknown memory usage type: %d\n", work->mem_usage);
    }
}

void work_run(work_t* work, long micros) {
    void* ptr = work_run_getmem(work);
    switch (work->cpu_usage) {
        case cpu_idle:
            work_run_idle(work->id, micros);
            break;
        case cpu_usr:
            work_run_usrcpu(work->id, micros);
            break;
        case cpu_sys:
            work_run_syscpu(work->id, micros);
            break;
        default:
            ERROR("Unknown CPU usage type: %d\n", work->cpu_usage);
    }
    work_run_freemem(work, ptr);
}

void work_explain(work_t* work) {
    EXPLAIN("work %d: ", work->id);
    switch (work->cpu_usage) {
        case cpu_idle:
            EXPLAIN("sleeps all the time keeping CPU idle");
            break;
        case cpu_usr:
            EXPLAIN("loads 1 of your %d CPUs with calculations", cpucount());
            break;
        case cpu_sys:
            EXPLAIN("loads 1 of your %d CPUs writing to a file", cpucount());
            break;
    }
    if (work->mem_size) {
        EXPLAIN(", uses %ld bytes of memory", work->mem_size);
    }
    EXPLAIN("\n");
}

long mem_usage(int threads, work_t* works) {
    long mem = 0;
    int i;
    for (i = 0; i < threads; ++i) {
        mem += works[i].mem_size;
    }
    return mem;
}

int usrcpu_usage(int threads, work_t* works) {
    int usrworks = 0;
    int i;
    for (i = 0; i < threads; ++i) {
        if (works[i].cpu_usage == cpu_usr) {
            ++usrworks;
        }
    }
    return usrworks;
}

int syscpu_usage(int threads, work_t* works) {
    int sysworks = 0;
    int i;
    for (i = 0; i < threads; ++i) {
        if (works[i].cpu_usage == cpu_sys) {
            ++sysworks;
        }
    }
    return sysworks;
}

int cpucount() {
    int cpus = sysconf(_SC_NPROCESSORS_ONLN);
    return cpus <= 0? 1 : cpus;
}

void mem2str(char* buf, long bytes) {
    sprintf(buf, "%ld bytes", bytes);
}

struct timeval start_time;

void print_run_time() {
    struct timeval current_time;
    gettimeofday(&current_time, 0);
    long seconds = current_time.tv_sec - start_time.tv_sec;
    printf("%ld:%02ld - ", seconds / 60, seconds % 60);
}
