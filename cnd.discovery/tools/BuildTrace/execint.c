/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/* 
 * File:   execint.c
 * Author: ll155635
 *
 * Created on July 11, 2010, 3:10 PM
 */

#include <dlfcn.h>
#include <limits.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
//#include <unistd.h>
#include <sys/stat.h>

#ifdef TRACE
#define LOG(args...) fprintf(stderr, ## args)
#else
#define LOG(...)
#endif

/****************************************************************/

static FILE* flog = NULL;
static char* filter[100];
static int filter_sz = 0;

static int comparator(const void * elem1, const void * elem2) {
    const char* str1 = *(const char**) elem1;
    const char* str2 = *(const char**) elem2;
    return strcmp(str1, str2);
}

static int init() {
    /* This function is not reenterable!!! TODO: introduce locking */
    static int interpose_init = 0;
    if (interpose_init != 0)
        return interpose_init;

    char* env_map = getenv("__CND_TOOLS__");
    char* env_log = getenv("__CND_BUILD_LOG__");

    if (env_map == NULL) {
        LOG("\n>>>ERROR: __CND_TOOLS__ is not set!!!\n");
        return interpose_init = -1;
    }
    if (env_log == NULL) {
        LOG("\n>>>ERROR: __CND_BUILD_LOG__ is not set!!!\n");
        return interpose_init = -1;
    }

    flog = fopen(env_log, "a");

    if (flog == NULL) {
        LOG("\n>>>ERROR: can not open%s!!!\n", env_log);
        return interpose_init = -1;
    }

    LOG("\n>>>NBBUILD: TOOLS=%s\n\tLOG=%s\n", env_map, env_log);

    char * token;
    int i = 0;
    for (token = strtok(env_map, ":");
            token;
            token = strtok(NULL, ":")) {
        if (strlen(token) == 0) {
            LOG("\n>>>WARN: TOOLS list contains empty values!!!\n");
            continue;
        }
        char* tail = strrchr(token, '/');
        if (tail) {
            LOG("\n>>>WARN: TOOLS list contains slashes - %s!!!\n", token);
            if (strlen(tail) == 0) {
                LOG("\n>>>WARN: TOOLS list contains directory - %s!!!\n", token);
                continue;
            }
            token = tail;
        }
        filter[i] = strdup(token);
        i++;
        filter_sz++;
    }

    if (filter_sz == 0)
        return interpose_init = -1;

    qsort(filter, filter_sz, sizeof (filter[0]), comparator);

    LOG("\n>>>NBBUILD: INIT DONE\n");

    return interpose_init = 1;
}

static void __logprint(const char* fname, char *const argv[], ...) {

    if (init() < 0) return;

    char* key = strrchr(fname, '/');
    if (key == NULL)
        key = fname;
    else
        key++;

    LOG("\n>>>NBBUILD: key = %s\n", key);

    char** found = bsearch(&key, filter, filter_sz, sizeof (filter[0]), comparator);

    if (found) {
        LOG("\n>>>NBBUILD: found %s\n", *found);
        //FILE* log = stderr;
        fprintf(flog, "called: %s\n", fname);
        char *buf = malloc(1024);
        getcwd(buf, 1024);
        fprintf(flog, "\t%s\n", buf);
        free(buf);
        char** par = (char**) argv;
        for (; *par != 0; par++)
            fprintf(flog, "\t%s\n", *par);
        fprintf(flog, "\n");
        fflush(flog);
    }
    return;
}

#define ORIG(func) _orig_##func
#define QUOTE(nm) #nm

// dirty hack
#define PARG , char** arg
#define PENV  , char** arg, const char** env
#define PVAR  , ...
#define ARG , arg
#define ENV , arg, env

#define INSTRUMENT(func, param, actual) \
int func (const char * p param) { \
    static int (* ORIG(func))(const char* p param) = NULL; \
    INIT(func); \
    LOG(">>>EXECINT: %s called. PATH=%s\n", QUOTE(func), p); \
    __logprint(p actual); \
    int ret = ORIG(func) (p actual); \
    LOG(">>>EXECINT: %s  returned\n", QUOTE(func)); \
    return ret; \
}

#define INIT(func) \
    if(!ORIG(func)) { \
        ORIG(func) = (typeof(ORIG(func)))dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(ORIG(func) && ORIG(func)==func) \
            ORIG(func) = (typeof(ORIG(func)))dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(!ORIG(func)) \
            ORIG(func) = (typeof(ORIG(func)))dlsym((void*)0 /*RTLD_DEFAULT*/, QUOTE(func)); \
    }

#define GETENV

INSTRUMENT(execv, PARG, ARG)
INSTRUMENT(execve, PENV, ENV)
INSTRUMENT(execvp, PARG, ARG)

#define RETURN(f) return f(name, (char **)first)

#define CONVERT(from_func, to_func) \
int from_func(char *name, ...) { \
    va_list args; \
    char**  first; \
    char**  env; \
    va_start(args, name); \
    first = (char**)args; \
    GETENV; \
    va_end(args); \
    LOG(">>>EXECINT: %s converted to %s\n", QUOTE(from_func), QUOTE(to_func)); \
    RETURN(to_func); \
}

CONVERT(execl, execv)
CONVERT(execlp, execvp)

#undef RETURN
#undef GETENV
#define GETENV \
       while (va_arg(args, char *) != (char *)0) {;};\
       env = va_arg(args, char **)
#define RETURN(f) return f(name, (char **)first, (const char**)env)

CONVERT(execle, execve)

static void
__attribute((constructor))
init_function(void) {
}

static void
__attribute((destructor))
fini_function(void) {
    if (flog) {
        fclose(flog);
        LOG("log closed\n");
    }
    int i = 0;
    for (; i < filter_sz; i++)
        free(filter[i]);
}
