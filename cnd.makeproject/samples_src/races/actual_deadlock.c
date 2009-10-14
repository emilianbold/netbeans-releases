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

int var1 = 0;
int var2 = 0;
pthread_mutex_t mutex1 = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mutex2 = PTHREAD_MUTEX_INITIALIZER;

void *thread1(void *arg) {
    int* p = (int*)arg;
    if (*p == 1) {
        pthread_mutex_lock(&mutex1);
        var1 = 1;
        sleep(1); // or some big code
        pthread_mutex_lock(&mutex2);
        var2 = 1;
        pthread_mutex_unlock(&mutex2);
        pthread_mutex_unlock(&mutex1);
    } else {
        pthread_mutex_lock(&mutex2);
        var2 = 2;
        sleep(1); // or some big code
        pthread_mutex_lock(&mutex1);
        var1 = 2;
        pthread_mutex_unlock(&mutex1);
        pthread_mutex_unlock(&mutex2);
    }
    return NULL;
}

int main() {
    pthread_t t1, t2;
    int arg1 = 1, arg2 = 2;
    printf("Program contains deadlock\n");
    pthread_create(&t1, NULL, thread1, &arg1);
    pthread_create(&t2, NULL, thread1, &arg2);
    pthread_join(t1, NULL);
    pthread_join(t2, NULL);

    return (EXIT_SUCCESS);
}
