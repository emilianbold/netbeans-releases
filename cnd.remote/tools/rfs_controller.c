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
#include <alloca.h>

#include "rfs_protocol.h"
#include "rfs_util.h"
#include "rfs_filedata.h"

 

static int emulate = false;

typedef struct connection_data {
    int sd;
    struct sockaddr_in pin;
} connection_data;

static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

static void serve_connection(void* data) {
    connection_data *conn_data = (connection_data*) data;
    trace("New connection from  %s:%d sd=%d\n", inet_ntoa(conn_data->pin.sin_addr), ntohs(conn_data->pin.sin_port), conn_data->sd);

    const int maxsize = PATH_MAX + 32;
    char buffer[maxsize];
    struct package *pkg = (struct package *) &buffer;

    int first = true;
    char requestor_id[32] = "-1";

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
        if (first ? (pkg->kind != pkg_handshake) : (pkg->kind != pkg_request && pkg->kind != pkg_written)) {
            fprintf(stderr, "prodocol error: unexpected %s from %s sd=%d\n", pkg_kind_to_string(pkg->kind), requestor_id, conn_data->sd);
            break;
        }

        if (first) {
            first = false;
            strncpy(requestor_id, pkg->data, sizeof requestor_id);
            continue;
        }

        const char* filename = pkg->data;
        file_data *fd = find_file_data(filename);

        const char LC_PROTOCOL_REQUEST = 'r';
        const char LC_PROTOCOL_WRITTEN = 'w';

        if (pkg->kind == pkg_written) {
            if (fd == NULL) {
                trace("File %s is unknown - nothing to uncontrol\n", filename);
            } else if (fd->state == MODIFIED) {
                trace("File %s already reported as modified\n", filename);
            } else {
                fd->state = MODIFIED;
                trace("File %s sending uncontrol request to LC\n", filename);
                // TODO: this is a very primitive sync!
                pthread_mutex_lock(&mutex);
                fprintf(stdout, "%c %s\n", LC_PROTOCOL_WRITTEN, filename);
                fflush(stdout);
                pthread_mutex_unlock(&mutex);
            }
        } else { // pkg->kind == pkg_request
            char response[64];
            response[1] = 0;
            if (fd != NULL) {
                switch (fd->state) {
                    case TOUCHED:
                        trace("File %s state %c - requesting LC\n", filename, (char) fd->state);
                        /* TODO: this is a very primitive sync!  */
                        pthread_mutex_lock(&mutex);

                        fprintf(stdout, "%c %s\n", LC_PROTOCOL_REQUEST, filename);
                        fflush(stdout);

                        if (emulate) {
                            response[0] = response_ok;
                        } else
                        fgets(response, sizeof response, stdin);
                        fd->state = (response[0] == response_ok) ? COPIED : ERROR;
                        pthread_mutex_unlock(&mutex);
                        trace("File %s state %c - got from LC %s, replying %s\n", filename, (char) fd->state, response, response);
                        break;
                    case COPIED:    // fall through
                    case UNCONTROLLED:
                    case MODIFIED:
                        response[0] = response_ok;
                        trace("File %s state %c - uncontrolled/modified/copied, replying %s\n", filename, (char) fd->state,  response);
                        break;
                    case ERROR:
                        response[0] = response_failure;
                        trace("File %s state %c - old error, replying %s\n", filename, (char) fd->state, response);
                        break;
                    case INITIAL:   // fall through
                    case DIRECTORY: // fall through
                    case PENDING:   // fall through
                    default:
                        response[0] = response_failure;
                        trace("File %s state %c - unexpected state, replying %s\n", filename, (char) fd->state, response);
                        break;
                }
            } else {
                response[0] = response_ok;
                trace("File %s: state n/a, replying: %s\n", filename, response);
            }

            response[1] = 0;
            enum sr_result send_res = pkg_send(conn_data->sd, pkg_reply, response);
            if (send_res == sr_failure) {
                perror("send");
            } else if (send_res == sr_reset) {
                perror("send");
            } else { // success
                trace("reply for %s sent to %s sd=%d\n", filename, requestor_id, conn_data->sd);
            }
        }
    }
    close(conn_data->sd);
    trace("Connection to %s:%d (%s) closed sd=%d\n", inet_ntoa(conn_data->pin.sin_addr), ntohs(conn_data->pin.sin_port), requestor_id, conn_data->sd);
}

static int _mkdir(const char *dir, int mask) {
    char tmp[1024];
    char *p = 0;
    size_t len;

    snprintf(tmp, sizeof (tmp), "%s", dir);
    len = strlen(tmp);
    if (tmp[len - 1] == '/') {
        tmp[len - 1] = 0;
    }
    for (p = tmp + 1; *p; p++) {
        if (*p == '/') {
            *p = 0;
            int rc = mkdir(tmp, mask);
            if (rc != 0) {
                // TODO: report errors
                trace("\t\terror creating dir %s: rc=%d\n", tmp, rc);
            }
            *p = '/';
        }
    }
    int rc = mkdir(tmp, mask);
    if (rc != 0) {
        // TODO: report errors
        trace("\t\terror creating dir %s: rc=%d\n", tmp, rc);
    }
    return rc;
}

static int create_dir(const char* path) {
    trace("\tcreating dir %s\n", path);
    int rc = _mkdir(path, 0700); // TODO: error processing
    if (rc != 0) {
        // TODO: report errors
        trace("\t\terror creating dir %s: rc=%d\n", path, rc);
    }
    return true; // TODO: check 
}

static int create_file(const char* path, int size) {
    trace("\tcreating file %s %d\n", path, size);
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0700);
    if (fd > 0) { // // TODO: error processing
        if (size > 0) {
            lseek(fd, size-1, SEEK_SET);
            char space = '\n';
            int written = write(fd, &space, 1);
            if (written != 1) {
                report_error("Error writing %s: %d bytes written\n", path, written);
                return false;
            }
        }
        if (close(fd) != 0) {
            report_error("error closing %s (fd=%d)\n", path, fd);
            return false;
        }
    } else {
        report_error("Error opening %s: %s\n", path, strerror(errno));
        return false;
    }
    return true;
}

static int touch_file(const char* path,  int size) {
    if (create_file(path, size)) {
        return true;
    } else {
        report_error("can not create proxy file %s: %s\n", path, strerror(errno));
        return false;
    }
}

static int file_exists(const char* path, int size) {
    struct stat stat_buf;
    if (stat(path, &stat_buf) == -1) {
        if (errno != ENOENT) {
            report_error("Can't check file %s: %s\n", path, strerror(errno));
        }
        return false;
    } else {
        return true;
    }
}

static enum file_state char_to_state(char c) {
    switch (c) {
        case INITIAL:
        case TOUCHED:
        case COPIED:
        case ERROR:
        case UNCONTROLLED:
        case MODIFIED:
        case DIRECTORY:
        case INEXISTENT:
            return c;
        default:
            return -1;
    }
}

static int scan_line(const char* buffer, int bufsize, enum file_state *state, int *file_size, const char **path) {
    *state = char_to_state(*buffer);
    if (*state == -1) {
        return false;
    }
    if (*state == DIRECTORY) { // directory
        // format is as in printf("D %s", path)
        *path = buffer + 2;
        *file_size = 0;
        return true;
    } else {
        // format is as in printf("%c %d %s", kind, length, path)
        const char* filename = buffer+2;
        while (filename < buffer + bufsize - 1 && *filename && *filename != ' ') {
            if (!isdigit(*filename)) {
                return false;
            }
            filename++;
        }
        // we should necessarily stay on ' '
        if (filename < buffer + bufsize - 1 && *filename == ' ') {
            filename++; // skip space after size
        } else {
            return false;
        }
        *file_size = atoi(buffer+2);
        *path = filename;
    }
}

typedef struct file_elem {
    struct file_elem* next;
    char filename[]; // have to be the last field
} file_elem;

/**
 * adds info about new file to the tail of the list
 */
static file_elem* add_file_to_list(file_elem* tail, const char* filename) {
    trace("File %s is added to the list to be send to LC as not yet copied files\n", filename);
    int namelen = strlen(filename);
    int size = sizeof(file_elem) + namelen + 1;
    file_elem *fe = (file_elem*) malloc(size);
    fe->next = NULL;
    strcpy(fe->filename, filename);
    if (tail != NULL) {
        tail->next = fe;
    }
    return fe;
}

static void free_file_list(file_elem* list) {
    while (list != NULL) {
        file_elem* next = list->next;
        free(list);
        list = next;
    }
}
/**
 * Reads the list of files from the host IDE runs on,
 * creates files, fills internal file table
 */
static int init_files() {
    trace("Files list initialization\n");
    int bufsize = PATH_MAX + 32;
    char buffer[bufsize];
    int success = false;
    file_elem* list = NULL;
    file_elem* tail = NULL;
    start_adding_file_data();
    while (1) {
        fgets(buffer, bufsize, stdin);
        if (buffer[0] == '\n') {
            success = true;
            break;
        }
        trace("\tFile init: %s", buffer); // no trailing LF since it's in the buffer
        // remove trailing LF
        char* lf = strchr(buffer, '\n');
        if (lf) {
            *lf = 0;
        }
        if (strchr(buffer, '\r')) {
            report_error("prodocol error: unexpected CR: %s\n", buffer);
            return false;
        }

        enum file_state state;
        int file_size;
        const char *path;

        scan_line(buffer, bufsize, &state, &file_size, &path);

        if (state == -1) {
            report_error("prodocol error: %s\n", buffer);
            break;
        } else if (state == DIRECTORY) { // directory
            create_dir(path);
        } else { // plain file
            int touch = false;
            if (state == INITIAL) {
                touch = true;
            } else if (state == COPIED || state == TOUCHED) {
                touch = !file_exists(path, file_size);
                if (state == COPIED && touch) {
                    // inform local controller that he is not right about copied status of file
                    tail = add_file_to_list(tail, path);
                    if (list == NULL) {
                        list = tail;
                    }
                }
            } else if (state = UNCONTROLLED || state == INEXISTENT) {
                // nothing
            } else {
                report_error("prodocol error: %s\n", buffer);
            }

            enum file_state new_state = state;
            if (touch) {
                if (touch_file(path, file_size)) {
                    new_state = TOUCHED;
                } else {
                    new_state = ERROR;
                }
            }

            if (*path == '/') {
                add_file_data(path, new_state);
            } else {
                char real_path [PATH_MAX];
                if (normalize_path(path, real_path, sizeof real_path)) {
                    add_file_data(real_path, new_state);
                } else {
                    report_unresolved_path(path);
                }
            }
        }
    }
    stop_adding_file_data();
    trace("Files list initialization done\n");
    if (success) {
        // send info about touched files which were passed as copied files
        tail = list;
        while (tail != NULL) {
            fprintf(stdout, "t %s\n", tail->filename);
            fflush(stdout);
            tail = tail->next;
        }
        free_file_list(list);
        // empty line as indication of finished files list
        fprintf(stdout, "\n");
        fflush(stdout);
    }
    return success;
}

int main(int argc, char* argv[]) {
    init_trace_flag("RFS_CONTROLLER_TRACE");
    trace_startup("RFS_C", "RFS_CONTROLLER_LOG", argv[0]);
    int port = default_controller_port;
    if (argc > 1) {
        port = atoi(argv[1]);
    }
    // auto mode for test purposes
    if (argc > 2 && strcmp(argv[2], "emulate") == 0) {
        emulate = true;
    }
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

    if (!init_files()) {
        report_error("Error when initializing files\n");
        exit(8);
    }

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
    // the code below is unreachable, so I commented it out
    // TODO: (?) more accurate shutdon?
    // close(sd);
    // trace_shutdown();
}
