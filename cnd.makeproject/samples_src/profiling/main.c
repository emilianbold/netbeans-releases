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
#include "common.h"
#include "barrier.h"
#include "join.h"
#include "mutex.h"
#include "parallel.h"
#include "rwlock.h"
#include "spinlock.h"
#include "sequential.h"

static work_t works[64] = {
    { 0, cpu_sys, mem_none, 0 }
};

int main(int argc, char** argv) {

    int i, work_count = MAX(cpucount(), 2);
    for (i = 1; i < work_count; ++i) {
        works[i].id = i;
        works[i].cpu_usage = cpu_usr;
        works[i].mem_usage = mem_malloc;
        works[i].mem_size = 10000 * i;
    }

    msg_levels = msg_explain | msg_trace;

    sequential_demo(work_count, works, 10);

    parallel_demo(work_count, works, 10);

    mutex_demo(work_count, works, 10);

//    rwlock_demo(work_count / 2, work_count - work_count / 2, works, 20);
//    barrier_demo(work_count, works);
//    join_demo(work_count, works);
//    spinlock_demo(work_count, works, 20);

    return (EXIT_SUCCESS);
}
