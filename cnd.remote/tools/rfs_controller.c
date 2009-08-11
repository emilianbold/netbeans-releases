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

        file_data *fd = find_file_data(request);
        
        if (fd->state == file_state_pending) {

            /* TODO: this is a very primitive sync!  */
            pthread_mutex_lock(&mutex);

            trace("Request: %s (size=%d)\n", request, size);
            fprintf(stdout, "%s\n", request);
            fflush(stdout);

            #if TRACE
            if (emulate) {
                response[0] = response_ok;
                response[1] = 0;
            } else {
                memset(response, 0, sizeof (response));
                gets(response);
            }
            #endif
            //strcpy(response, "ok");
            fd->state = (response[1] == response_ok) ? file_state_ok : file_state_error;
            pthread_mutex_unlock(&mutex);
        } else {
            if (fd->state == file_state_ok) {
                response[0] = response_ok;
            } else {
                response[0] = response_failure;
            }
            response[1] = 0;
        }

        trace("Reply: %s\n", response);
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

int main(int argc, char* argv[]) {
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
        perror("Port");
        trace("Searching for available port...\n", port);
        int bind_rc;
        do {
            sin.sin_port = htons(++port);
            trace("\t%d...\n", port);
            bind_rc = bind(sd, (struct sockaddr *) &sin, sizeof (sin));
        } while (bind_rc == -1 && port < 99999);
        if (bind_rc == -1) {
            perror("port");
            exit(2);
        }
    };

    fprintf(stdout, "PORT %d\n", port);
    fflush(stdout);
    
    /* show that we are willing to listen */
    if (listen(sd, 5) == -1) {
        perror("listen");
        exit(1);
    }

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
}
