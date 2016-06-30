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
#ifdef MINGW
#include <process.h>
#else
#include <spawn.h>
#include <sys/wait.h>
#endif
#include <unistd.h>
#include <limits.h>
#ifdef WINDOWS
#define PATH_SEPARATOR ";"
#define FILE_SEPARATOR_CHAR '\\'
#define FILE_SEPARATOR_STRING "\\"
#else
#define PATH_SEPARATOR ":"
#define FILE_SEPARATOR_CHAR '/'
#define FILE_SEPARATOR_STRING "/"
#endif
#define MAGIC "echo magic"
extern char **environ;
#define COPY(x) x x x x x x x x x x
char *real_binary = COPY(COPY(MAGIC));

void prependPath(char* path) {
    char** e = environ;
    while (e) {
        char *buf = malloc(strlen(*e) + 1);
        strcpy(buf, *e);
        char* eq = strchr(buf, '=');
        if (eq != NULL) {
            *eq = 0;
            if (strcasecmp("PATH", buf) == 0) {
                char* old_path = getenv(buf);
                char *tool_path = malloc(strlen(path) + 1);
                strcpy(tool_path, path);
                char* key;
                key = strrchr(tool_path, FILE_SEPARATOR_CHAR);
                if (key != NULL) {
                    *key = 0;
                }
                char *new_path = malloc(strlen(tool_path) + 1 + strlen(old_path) + 1);
#ifdef WINDOWS
                sprintf(new_path, "%s;%s", tool_path, old_path); 
#else
                sprintf(new_path, "%s:%s", tool_path, old_path); 
#endif
#ifdef MINGW
                char *path_macro = malloc(strlen(buf) + 1 + strlen(new_path) + 1);
                sprintf(path_macro, "%s=%s", buf, new_path); 
                putenv(path_macro);
                free(path_macro);
#else
                setenv(buf, new_path, 1);
#endif
                free(new_path);
                free(tool_path);
                free(buf);
                break;
            }
        }
        free(buf);
        e++;
    }
}

int main(int argc, char**argv) {
    char *pattern = MAGIC;
    char *place = real_binary;
    int changed = 0;
    while(*pattern) {
        if (*place != *pattern) {
            changed = 1;
            break;
        }
        pattern++;
        place++;
    }
    if (!changed) {
        printf("Real compiler is not set\n");
        return -1;
    }

    argv[0] = real_binary;
    char* log = getenv("__CND_BUILD_LOG__");
    if (log != NULL) {
        FILE* flog = fopen(log, "a");
        if (flog != NULL) {
            fprintf(flog, "called: %s\n", real_binary);
            char *buf = malloc(MY_MAX_PATH + 1);
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
    }
    prependPath(real_binary);
#ifdef MINGW
    return spawnv(P_WAIT, real_binary, argv);
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
