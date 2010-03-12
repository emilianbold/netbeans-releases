/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

#include <stdio.h>
#include <unistd.h>
#include <limits.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>

typedef int bool;

enum {
    true = 1,
    false = 0
};

extern bool trace_flag;

static void init_trace_flag(const char* env_var) {
    char *env = getenv(env_var);
    trace_flag = env && *env == '1';
}

void report_error(const char *format, ...);

static void report_unresolved_path(const char* path) {
    char pwd[PATH_MAX + 1];
    getcwd(pwd, sizeof pwd);
    report_error("Can not resolve path: %s  cwd: %s\n", path, pwd);
}


#define trace(...) if (trace_flag) { _trace(__VA_ARGS__); }
void _trace(const char *format, ...);

#define trace_startup(prefix, env_var, binary) if (trace_flag) { _trace_startup(prefix, env_var, binary); }
void _trace_startup(const char* prefix, const char* env_var, const char* binary);

#define trace_shutdown() if (trace_flag) { _trace_shutdown(); }
void _trace_shutdown();

#define trace_unresolved_path(path) if (trace_flag) { _trace_unresolved_path(path); }
static void _trace_unresolved_path(const char* path) {
    if (trace_flag) {
        char pwd[PATH_MAX + 1];
        getcwd(pwd, sizeof pwd);
        trace("Can not resolve path: %s  pwd: %s\n", path, pwd);
    }
}

#define dbg_sleep(time) if (trace_flag) { _dbg_sleep(time); }
static void _dbg_sleep(int time) {
    if (trace_flag) {
        trace("Sleeping %d sec...\n", time);
        sleep(time);
        trace("Awoke\n");
    }
}

/*
static char *normalize_path(const char *path, char *buffer, int max_size) {
    if (path == NULL || buffer == NULL) {
        errno = EINVAL;
        return NULL;
    }
    const char *src = path; // points to the current char in the source path
    char *dst = buffer; // points to the next char in the destination path
    char* limit = buffer + max_size;
    if (*path != '/') {
        if (getcwd(buffer, max_size)) {
            int len = strlen(buffer);
            dst = buffer + len;
            if (dst + 1 >= limit) {
                errno = ENAMETOOLONG;
                return NULL;
            }
            *(dst++) = '/';
        } else {
            return NULL;
        }
    }
    while (*src) {
        if (*src == '.' && (src == path || *(src-1) == '/')) {
            if (*(src+1) == '.' && *(src+2) == '/') {
                // it's "/../"
                src += 3;
                dst--; // point the last added one
                if (*dst == '/' && dst > buffer) {
                    dst--;
                }
                while (dst > buffer && *dst != '/') {
                    dst--;
                }
                dst++;
                continue;
            } else if (*(src+1) == '/') {
                // it's "/./" - skip '.' and '/'
                src += 2;
                continue;
            }
        }
        if (dst + 1 >= limit) {
            errno = ENAMETOOLONG;
            return NULL;
        }
        *(dst++) = *(src++);
    }
    if (*dst == '/') {
        dst--;
    }
    if (dst + 1 >= limit) {
        errno = ENAMETOOLONG;
        return NULL;
    }
    *dst = 0;
    trace("normalize: %s -> %s\n", path, buffer);
    return dst;
}
*/
