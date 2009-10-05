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

#include <stdlib.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <stdio.h>
#include <string.h>
#include <memory.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <pthread.h>
#include <ctype.h>
#include <errno.h>
#include <unistd.h>
#include <limits.h>
#include <sys/stat.h>

 #include <netinet/in.h>
 #include <arpa/inet.h>

#include "rfs_protocol.h"
#include "rfs_util.h"
#include "rfs_filedata.h"

 

#if TRACE
static int emulate = false;
#endif

typedef struct connection_data {
    int sd;
    struct sockaddr_in pin;
} connection_data;

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

static void serve_connection(void* data) {
    connection_data *conn_data = (connection_data*) data;
    trace("New connection from  %s:%d sd=%d\n", inet_ntoa(conn_data->pin.sin_addr), ntohs(conn_data->pin.sin_port), conn_data->sd);

    const int maxsize = PATH_MAX + 32;
    char buffer[maxsize];
    struct package *pkg = (struct package *) &buffer;

    int first = true;
    char requestor_id[32];

    while (1) {
        trace("Waiting for a data to arrive from %s, sd=%d...\n", requestor_id, conn_data->sd);
        errno = 0;
        enum sr_result recv_res = pkg_recv(conn_data->sd, pkg, maxsize);
        if (recv_res == sr_reset) {
            trace("Connection sd=%d reset by peer => normal termination\n", conn_data->sd);
            break;
        } else if (recv_res == sr_failure) {
            if (errno != 0) {
                perror("error getting message");
            }
            break;
        }

        trace("Request (%s): %s sd=%d\n", pkg_kind_to_string(pkg->kind), pkg->data, conn_data->sd);
        enum pkg_kind expected_kind = first ? pkg_handshake : pkg_request;
        if (pkg->kind != expected_kind) {
            fprintf(stderr, "prodocol error: got %s instead of %s from %s sd=%d\n", pkg_kind_to_string(pkg->kind), pkg_kind_to_string(expected_kind), requestor_id);
            break;
        }

        if (first) {
            first = false;
            strncpy(requestor_id, pkg->data, sizeof requestor_id);
            continue;
        }

        char response[64];
        file_data *fd = find_file_data(pkg->data, true);

        if (fd != NULL && fd->state == file_state_pending) {
            /* TODO: this is a very primitive sync!  */
            pthread_mutex_lock(&mutex);

            fprintf(stdout, "%s\n", pkg->data);
            fflush(stdout);

            #if TRACE
                if (emulate) {
                    response[0] = response_ok;
                    response[1] = 0;
                } else
            #endif
            fgets(response, sizeof response, stdin);
            fd->state = (response[0] == response_ok) ? file_state_ok : file_state_error;
            pthread_mutex_unlock(&mutex);
            trace("Got reply=%s from sd=%d set %X->state to %d\n", response, conn_data->sd, fd, fd->state);
        } else {
            if (fd != NULL && fd->state == file_state_ok) {
                response[0] = response_ok;
            } else { // either fd == NULL or fd->state == file_state_error
                response[0] = response_failure;
            }
            response[1] = 0;
            trace("Already known; filled reply: %s\n", response);
        }

        enum sr_result send_res = pkg_send(conn_data->sd, pkg_reply, response);
        if (send_res == sr_failure) {
            perror("send");
        } else if (send_res == sr_reset) {
            perror("send");
        } else { // success
            trace("reply for %s sent to %s sd=%d\n", pkg->data, requestor_id, conn_data->sd);
        }        
    }
    close(conn_data->sd);
    trace("Connection to %s:%d (%s) closed sd=%d\n", inet_ntoa(conn_data->pin.sin_addr), ntohs(conn_data->pin.sin_port), requestor_id, conn_data->sd);
}

static void create_dir(const char* path) {
    trace("\tcreating dir %s\n", path);
    int rc = mkdir(path, 0700); // TODO: error processing
    if (rc != 0) {
        trace("\t\terror creating dir %s: rc=%d\n", path, rc);
    }
}

static void create_file(const char* path, int size) {
    trace("\tcreating file %s %d\n", path, size);
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0700);
    if (fd > 0) { // // TODO: error processing
        if (size > 0) {
            lseek(fd, size-1, SEEK_SET);
            char space = '\n';
            int written = write(fd, &space, 1);
            if (written != 1) {
                trace("\t\terror writing %s: %d bytes written\n", path, written);
            }
        }
        if (close(fd) != 0) {
            trace("\t\terror closing %s (fd=%d)\n", path, fd);
        }
    } else {
        trace("\t\terror opening %s\n", path);
    }
}

/**
 * Reads the list of files from the host IDE runs on,
 * creates files, fills internal file table
 */
static void init_files() {
    trace("Files list initialization\n");
    int bufsize = PATH_MAX + 32;
    char buffer[bufsize];
    while (1) {
        fgets(buffer, bufsize, stdin);
        if (buffer[0] == '\n') {
            break;
        }
        // remove trailing LF
        char* lf = strchr(buffer, '\n');
        if (lf) {
            *lf = 0;
        }
        if (*buffer == 'D') { // directory
            create_dir(buffer + 2);
        } else { // plain file
            // check that the line has proper format, otherwise we can got seg. fault
            // also find where the path begin
            char* path = buffer;
            while (path < buffer + bufsize - 1 && *path && *path != ' ') {
                if (!isdigit(*path)) {
                    fprintf(stderr, "prodocol error: %s\n", buffer);
                    return;
                }
                path++;
            }
            // we should necessarily stay on ' '
            if (path < buffer + bufsize - 1 && *path == ' ') {
                path++; // skip space after size
            } else {
                fprintf(stderr, "prodocol error: %s\n", buffer);
                return;
            }
            int size = atoi(buffer);
            create_file(path, size);
        }
    }
    trace("Files list initialization done\n");
}

int main(int argc, char* argv[]) {
    trace_startup("RFS_C", "RFS_CONTROLLER_LOG", argv[0]);
    int port = default_controller_port;
    if (argc > 1) {
        port = atoi(argv[1]);
    }
    #if TRACE
    // auto mode for test purposes
    if (argc > 2 && strcmp(argv[2], "emulate") == 0) {
        emulate = true;
    }
    #endif
    int sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd == -1) {
        perror("Socket");
        exit(1);
    }

    struct sockaddr_in sin;
    memset(&sin, 0, sizeof (sin));
    sin.sin_family = AF_INET;
    sin.sin_addr.s_addr = INADDR_ANY;
    sin.sin_port = htons(port);

    if (bind(sd, (struct sockaddr *) & sin, sizeof (sin)) == -1) {
        if (errno != EADDRINUSE) {
            perror("Error opening port: ");
            exit(2);
        }
        trace("Searching for available port...\n", port);
        int bind_rc;
        do {
            sin.sin_port = htons(++port);
            trace("\t%d...\n", port);
            bind_rc = bind(sd, (struct sockaddr *) &sin, sizeof (sin));
        } while (bind_rc == -1 && port < 99999);
        if (bind_rc == -1) {
            perror("port");
            exit(4);
        }
    };
    
    /* show that we are willing to listen */
    if (listen(sd, 5) == -1) {
        perror("listen");
        exit(1);
    }

    init_files();

    // print port later, when we're done with initializing files
    fprintf(stdout, "PORT %d\n", port);
    fflush(stdout);

    while (1) {
        /* wait for a client to talk to us */
        connection_data* conn_data = (connection_data*) malloc(sizeof (connection_data));
        int addrlen = sizeof (conn_data->pin);
        if ((conn_data->sd = accept(sd, (struct sockaddr *) & conn_data->pin, &addrlen)) == -1) {
            perror("accept");
            exit(1);
        }
        pthread_t thread;
        pthread_create(&thread, NULL /*&attr*/, (void *(*) (void *)) serve_connection, conn_data);
        pthread_detach(thread);
    }

    close(sd);
    trace_shutdown();
}
