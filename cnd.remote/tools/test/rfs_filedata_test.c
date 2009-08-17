#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>

#include "../rfs_filedata.h"

void *start_routine(void *data) {
    file_data *fd = (file_data*) data;
    printf("Start waiting on %s\n", fd->filename);
    //wait_on_file_data(fd);
    //printf("Done waiting on %s\n", fd->filename);
    return NULL;
}

static void test_trivial() {
    file_data *fd = find_file_data("/tmp/asd");
    pthread_t thread1, thread2;
    pthread_create(&thread1, NULL, &start_routine, fd);
    pthread_create(&thread2, NULL, &start_routine, fd);
    printf("Launched threads, sleeping\n");
    sleep(3);
    printf("Awoke\n");
    signal_on_file_data(fd);
    pthread_join(thread1, NULL);
    pthread_join(thread2, NULL);
    printf("Done\n");
}

static int test_tree_search_printing_visitor(file_data *fd, void* data) {
    #if !TRACE
    #error TRACE should be defined
    #endif
    printf("VISITOR: \"%s\" cnt=%d\n", fd->filename, fd->cnt);
}

static void print_tree() {
    visit_file_data(test_tree_search_printing_visitor, NULL);
}

static void fill_tree_from_file(const char* test_file_name, int trace) {
    FILE* file = fopen(test_file_name, "r");
    if (!file) {
        fprintf(stderr, "Error opening %s: ", test_file_name);
        perror(NULL);
        return;
    }
    int cnt = 0;
    char filename[1024];
    while (fgets(filename, sizeof(filename), file) != NULL) {
        // remove the trailing '\n'
        int len = strlen(filename);
        filename[len-1] = 0;
        if (trace) printf("Adding \"%s\"\n", filename);
        file_data *fd = find_file_data(filename);
        if (fd) {
            fd->cnt++;
            if (trace) printf("Got data for %s: %X %d\n", filename, fd, fd->cnt);
        } else {
            fprintf(stderr, "Error: find_file_data returned NULL for %s\n", filename);
        }
        cnt++;
    }
    printf("Added %d elements\n", cnt);
}

static void test_tree_search_single_thread(const char* test_file_name) {
    fill_tree_from_file(test_file_name, 0);
    print_tree();
}

void *test_tree_search_multy_thread_start_routine(void* data) {
    fill_tree_from_file((const char*) data, 0);
}
static void test_tree_search_multy_thread(char* test_file_name, int num_threads) {
    pthread_t threads[num_threads];
    int i;
    for (i = 0; i < num_threads; i++) {
        pthread_create(&threads[i], NULL, &test_tree_search_multy_thread_start_routine, test_file_name);
    }
    printf("Adding in threads...\n");
    sleep(3);
    printf("Awoke\n");
    for (i = 0; i < num_threads; i++) {
        pthread_join(threads[i], NULL);
    }
    print_tree();
}

int main(int argc, char** argv) {
    //test_tree_search_single_thread("/tmp/test_tree_search");
    test_tree_search_multy_thread("/tmp/test_tree_search", 10);
    return 0;
}
