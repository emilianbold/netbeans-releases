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
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>

#include "rfs_filedata.h"

static file_data *root = NULL;
pthread_mutex_t file_data_tree_mutex = PTHREAD_MUTEX_INITIALIZER;

static file_data *new_file_data(const char* filename, file_state state) {
    int namelen = strlen(filename);
    int size = sizeof(file_data) + namelen + 1;
    file_data *fd = (file_data*) malloc(size);
    pthread_mutex_init(&fd->cond_mutex, NULL);
    pthread_cond_init(&fd->cond, NULL);
    fd->state = state;
    strcpy(fd->filename, filename);
    fd->left = fd->right = NULL;
    #if DEBUG_FILE_DATA
    fd->cnt = 0;
    #endif
    return fd;
}

void wait_on_file_data(file_data *fd) {
    pthread_mutex_lock(&fd->cond_mutex);
    pthread_cond_wait(&fd->cond, &fd->cond_mutex);
    pthread_mutex_unlock(&fd->cond_mutex);
}

void signal_on_file_data(file_data *fd) {
    pthread_mutex_lock(&fd->cond_mutex);
    pthread_cond_signal(&fd->cond);
    pthread_mutex_unlock(&fd->cond_mutex);
}

static int visit_file_data_impl(file_data *fd, int (*visitor) (file_data*, void*), void *data) {
    if (fd) {
        if (visitor(fd, data) &&
                visit_file_data_impl(fd->left, visitor, data) &&
                visit_file_data_impl(fd->right, visitor, data)) {
            return 1;
        } else {
            return -1;
        }
    }
    return 1;
}

void visit_file_data(int (*visitor) (file_data*, void*), void *data) {
    visit_file_data_impl(root, visitor, data);
}

file_data *find_file_data(const char* filename) {
    file_data *result = NULL;
    pthread_mutex_lock(&file_data_tree_mutex);
    if (root) {
        file_data *curr = root;
        // a clumsy, but very straightforward tree search
        while (1) {
            int cmp = strcmp(filename, curr->filename);
            if (cmp == 0) {
                result = curr;
                break;
            } else if(cmp < 0) {
                if(curr->left) {
                    cmp = strcmp(filename, curr->left->filename);
                    if (cmp == 0) {
                        result = curr->left;
                        break;
                    } else if(cmp < 0) {
                        curr = curr->left;
                    } else { // cmp > 0
                        result = new_file_data(filename, file_state_pending);
                        result->left = curr->left; // right is nulled by new_file_data
                        curr->left = result;
                        break;
                    }
                } else {
                    curr->left = result = new_file_data(filename, file_state_pending);
                    break;
                }
            } else { // cmp > 0
                if (curr->right) {
                    cmp = strcmp(filename, curr->right->filename);
                    if (cmp == 0) {
                        result = curr->right;
                        break;
                    } else if(cmp < 0) {
                        result = new_file_data(filename, file_state_pending);
                        result->right = curr->right;
                        curr->right = result;
                        break;
                    } else { // cmp > 0
                        curr = curr->right;
                    }
                } else {
                    curr->right = result = new_file_data(filename, file_state_pending);
                    break;
                }
            }
        }
    } else {
        result = root = new_file_data(filename, file_state_pending);
    }
    pthread_mutex_unlock(&file_data_tree_mutex);
    return result;
}
