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

typedef int bool;

enum {
    true = 1,
    false = 0
};

void report_error(const char *format, ...);

static void report_unresolved_path(const char* path) {
    char pwd[PATH_MAX];
    getcwd(pwd, sizeof pwd);
    report_error("Can not resolve path: %s  cwd: %s\n", path, pwd);
}

#if TRACE
    void trace(const char *format, ...);
    void trace_startup(const char* prefix, const char* env_var, const char* binary);
    void trace_shutdown();
    static void trace_unresolved_path(const char* path) {
        char pwd[PATH_MAX];
        getcwd(pwd, sizeof pwd);
        trace("Can not resolve path: %s  pwd: %s: %s\n", path, pwd);
    }
    static void dbg_sleep(int time) {
        trace("Sleeping %d sec...\n", time);
        sleep(time);
        trace("Awoke\n");
    }
#else
    #define trace_startup(...)
    #define trace(...)
    #define trace_shutdown()
    #define trace_unresolved_path(...)
    #define dbg_sleep(...)
#endif
