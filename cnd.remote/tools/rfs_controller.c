#include <stdlib.h>

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
#include <sys/fcntl.h>

#define RFS_CONTROLLER 1 // rfs_utils.h needs this

#include "rfs_controller.h"
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

static void new_connection_start_function(void* data) {
    connection_data *conn_data = (connection_data*) data;
    trace("New connection from  %s:%d sd=%d\n", inet_ntoa(conn_data->pin.sin_addr), ntohs(conn_data->pin.sin_port), conn_data->sd);
    const int bufsize = 512;
    char request[bufsize];
    char response[bufsize];
    int size;
    while (1) {
        trace("Waiting for a data to arrive...\n");
        memset(request, 0, sizeof (request));
        size = recv(conn_data->sd, request, sizeof (request), 0);
        if (size == -1) {
            break;
        }
        if (size == 0) {
            break; // TODO: why is it 0??? Should be -1 and errno==ECONNRESET
        }

        file_data *fd = find_file_data(request, true);
        
        trace("Request: %s (size=%d)\n", request, size);
        if (fd != NULL && fd->state == file_state_pending) {

            /* TODO: this is a very primitive sync!  */
            pthread_mutex_lock(&mutex);

            fprintf(stdout, "%s\n", request);
            fflush(stdout);

            memset(response, 0, sizeof (response));
            #if TRACE
            if (emulate)
                response[0] = response_ok;
            else
            #endif
            gets(response);
            fd->state = (response[0] == response_ok) ? file_state_ok : file_state_error;
            pthread_mutex_unlock(&mutex);
            trace("Got reply: %s set %X->state to %d\n", response, fd, fd->state);
        } else {
            if (fd != NULL && fd->state == file_state_ok) {
                response[0] = response_ok;
            } else { // either fd == NULL or fd->state == file_state_error
                response[0] = response_failure;
            }
            response[1] = 0;
            trace("Already known; filled reply: %s\n", response);
        }
        
        if ((size = send(conn_data->sd, response, strlen(response), 0)) == -1) {
            perror("send");
        } else {
            trace("%d bytes sent\n", size);
        }
        
    }
    if (errno == ECONNRESET) {
        trace("Connection reset by peer => normal termination\n");
    } else if (errno != 0) {
        perror("error getting message");
    }
    close(conn_data->sd);
    trace("Connection to %s:%d closed\n", inet_ntoa(conn_data->pin.sin_addr), ntohs(conn_data->pin.sin_port));
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
    trace_startup("RFS_CONTROLLER_LOG");
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
        pthread_create(&thread, NULL, (void *(*) (void *)) new_connection_start_function, conn_data);
    }

    close(sd);
    trace_shutdown();
}
