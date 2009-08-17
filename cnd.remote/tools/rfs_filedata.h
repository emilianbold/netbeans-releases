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

#ifndef _RFS_FILEDATA_H
#define	_RFS_FILEDATA_H

#include <pthread.h>

#ifdef	__cplusplus
extern "C" {
#endif

typedef enum file_state {
    file_state_pending = 0,
    file_state_ok = 1,
    file_state_error = -1
} file_state;

typedef struct file_data {
    volatile file_state state;
    pthread_mutex_t cond_mutex;
    pthread_cond_t cond;
    struct file_data *left;
    struct file_data *right;
    #if TRACE
    int cnt;
    #endif
    char filename[];
} file_data;

/**
 * Finds file_data for the given file name;
 * if it does not exist, creates one, inserts it into the tree and
 * returns a reference to the newly inserted one
 */
file_data *find_file_data(const char* filename);

/**
 * Visits all file_data elements - calls function passed as a 1-st parameter
 * for each file_data element.
 * Two parameters are passed to the function on each call:
 * 1) current file_data
 * 2) pointer that is passed as 2-nd visit_file_data parameter
 * In the case function returns 0, the tree traversal is stopped
 */
void visit_file_data(int (*) (file_data*, void*), void*);

void wait_on_file_data(file_data *fd);
void signal_on_file_data(file_data *fd);

#ifdef	__cplusplus
}
#endif

#endif	/* _RFS_FILEDATA_H */

