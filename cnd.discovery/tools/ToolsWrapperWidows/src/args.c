/*
 * Copyright (c) 2009-2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#define MY_MAX_PATH (1024)
#ifdef WIN32
    #include <process.h>
#else
    #include <spawn.h>
#endif
#include <unistd.h>
#include <limits.h>

extern char **environ;

int main(int argc, char**argv) {
    const char* key;
    #ifdef WIN32
        key = strrchr(argv[0], '\\');
    #else
        key = strrchr(argv[0], '/');
    #endif
    if (key == NULL) {
        key = argv[0];
    } else {
        key++;
    }
    #ifdef WIN32
        char* dot = strrchr(key, '.');
        if (dot != NULL) {
            char *buf = malloc(strlen(key)+1);
            strcpy(buf, key);
            dot = strrchr(buf, '.');
            *dot = 0;
            key = buf;
        }
    #endif
    
    char* tool = NULL;
    char* tool_path_variable = NULL;
    if (strcmp(key, "cc") == 0 ||
        strcmp(key, "xgcc") == 0 ||
        strcmp(key, "clang") == 0 ||
        strcmp(key, "icc") == 0 || 
        strcmp(key, "gcc") == 0) {
        tool = getenv("__CND_C_TOOL__");
    } else if (strcmp(key, "CC") == 0 ||
               strcmp(key, "c++") == 0 ||
               strcmp(key, "clang++") == 0 ||
               strcmp(key, "icpc") == 0 ||
               strcmp(key, "cl") == 0 ||
               strcmp(key, "g++") == 0) {
        tool = getenv("__CND_CPP_TOOL__");
    } else {
        printf("Unsupported %s compiler", key);
        return -1;
    }
    if (tool == NULL) {
        printf("Set path to %s compiler in env variable %s", key, tool_path_variable);
        return -1;
    }
    argv[0] = tool;
    char* log = getenv("__CND_BUILD_LOG__");
    FILE* flog;
    if (log != NULL) {
        flog = fopen(log, "a");
    }
    if (flog != NULL) {
        fprintf(flog, "called: %s\n", tool);
        char *buf = malloc(MY_MAX_PATH+1);
        getcwd(buf, MY_MAX_PATH);
        fprintf(flog, "\t%s\n", buf);
        char** par = (char**) argv;
        for (; *par != 0; par++) {
            fprintf(flog, "\t%s\n", *par);
        }
        fprintf(flog, "\n");
        fflush(flog);
        fclose(flog);
    }
#ifdef WIN32
    return spawnv(P_WAIT,tool, argv);
#else
    pid_t pid;
    int status;
    status = posix_spawn(&pid, tool, NULL, NULL, argv, environ);
    if (status == 0) {
        if (waitpid(pid, &status, 0) != -1) {
            return status;
        } else {
            return -1;
        }
    } else {
        return status;
    }    
#endif
}
