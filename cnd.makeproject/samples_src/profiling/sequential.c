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

#include "sequential.h"

void sequential_demo(int work_count, work_t* works, int seconds_per_work) {
    PRINT("*** SEQUENTIAL DEMO ***\n\n");
    EXPLAIN("  I'm going to run %d works sequentially, one after another.\n", work_count);
    EXPLAIN("  Each work will run for %d seconds.\n\n", seconds_per_work);
    int i;
    for (i = 0; i < work_count; ++i) {
        work_explain(&works[i]);
        PAUSE("Press [Enter] to start this work...\n");
        REF("Estimated resource usage for the following %d seconds\n", seconds_per_work);
        REF("CPU usage:\n");
        REF("\tuser:   about %d%%\n", 100 * usrcpu_usage(1, &works[i]) / cpucount());
        REF("\tsystem: about %d%%\n", 100 * syscpu_usage(1, &works[i]) / cpucount());
        REF("Memory usage:\n");
        REF("\t%ld bytes\n", mem_usage(1, &works[i]));
        REF("Thread usage:\n");
        REF("\ttotal:  1\n");
        REF("\tlocked: 0\n\n");
        work_run(&works[i], seconds_per_work * MICROS_PER_SECOND);
        PRINT("\n");
    }
}
