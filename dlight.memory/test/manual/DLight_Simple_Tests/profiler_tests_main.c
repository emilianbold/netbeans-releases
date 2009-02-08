
#include <string.h>
#include <unistd.h> // sleep in SUN headers is here
#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <alloca.h>

#include "test_sync.h"
#include "test_write.h"
#include "test_sync_trivial.h"
#include "test_alloc.h"
#include "pi.h"
#include "test_dl.h"

typedef int boolean;
#define false 0
#define true 1

static boolean test_leak_malloc = false;
static boolean test_write_ = false;
static boolean test_leak_calloc = false;
static boolean test_leak_realloc = false;
static boolean test_sync = false;
static boolean test_sync_trivial = false;
static boolean test_pi = false;
static boolean test_dl_ = false;

static int cycles = 10;
static int initial_sleep = 5;
static int step_sleep = 1;

#define SLICE 10000

extern void test_sync_trivial_run(int step);

static void test(int step) {
    if (test_sync_trivial) {        
        test_sync_trivial_run(step);
    }
    if (test_write_) {
        test_write(512);
    }
    if (test_leak_malloc) {
        leak_malloc(SLICE, step);
    }
    if (test_leak_calloc) {
        leak_calloc(SLICE, step);
    }
    if (test_leak_realloc) {
        leak_realloc(SLICE, step);
    }
    if (test_sync) {
        test_sync_step(step);
    }
    if (test_dl_) {
        test_dl(step);
    }
    if (test_pi) {
        int num_steps = 1000000;
        printf("Calculating PI (%d steps)...\n", num_steps);
        calc_pi(num_steps);
    }
}

static boolean process_flag(char flag) {
    switch (flag) {
        case 'w':
            test_write_ = true;
            break;
        case 'm':
            test_leak_malloc = true;
            break;
        case 'c':
            test_leak_calloc = true;
            break;
        case 'r':
            test_leak_realloc = true;
            break;
        case 's':
            test_sync = true;
            break;
        case 'd':
            test_dl_ = true;
            break;
        case 'S':
            test_sync_trivial = true;
            break;
        case 'p':
            test_pi = true;
            break;
        default:
            printf("Unknown option -%c\n", flag);
            return false;
    }
    return true;
}

static boolean process_flags(int argc, char** argv) {
    int arg_idx;
    for (arg_idx = 1; arg_idx < argc; arg_idx++) {
        if (argv[arg_idx][0] == '-') {
            if (argv[arg_idx][1] == '-') {
                if (strcmp(argv[arg_idx], "--sleep") == 0) {
                    if (arg_idx+1 < argc) {
                        arg_idx++;
                        step_sleep = atoi(argv[arg_idx]);
                        if (step_sleep < 0) {
                            printf("--sleep option should be followed by a non-negative numeric value in seconds\n");
                            return false;
                        }
                    } else {
                        printf("--sleep option should be followed by a positive numeric value in seconds\n");
                        return false;
                    }
                }
                else if (strcmp(argv[arg_idx], "--isleep") == 0) {
                    if (arg_idx+1 < argc) {
                        arg_idx++;
                        initial_sleep = atoi(argv[arg_idx]);
                        if (step_sleep < 0) {
                            printf("--isleep option should be followed by a non-negative numeric value in seconds\n");
                            return false;
                        }
                    } else {
                        printf("--isleep option should be followed by a positive numeric value in seconds\n");
                        return false;
                    }
                }
                else if (strcmp(argv[arg_idx], "--cycles") == 0) {
                    if (arg_idx+1 < argc) {
                        arg_idx++;
                        cycles = atoi(argv[arg_idx]);
                        if (cycles <= 0) {
                            printf("--cycles option should be followed by a positive numeric value in seconds\n");
                            return false;
                        }
                    } else {
                        printf("--sleep option should be followed by a positive numeric value in seconds\n");
                        return false;
                    }
                } else {
                    printf("Unknown option %s\n", argv[arg_idx]);
                    return false;
                }
            } else {
                int len = strlen(argv[arg_idx]);
                int char_idx;
                for (char_idx = 1; char_idx < len; char_idx++) {
                    if (!process_flag(argv[arg_idx][char_idx])) {
                        return false;
                    }
                }
            }
        }
    }
    if(test_leak_malloc || test_leak_calloc || test_leak_realloc || test_sync || test_sync_trivial || test_pi || test_write_ || test_dl_) {
        return true;
    } else {
        test_leak_malloc = test_leak_calloc = test_leak_realloc = test_sync = test_sync_trivial = test_pi = test_dl_ = true;
        printf("No tests specified.\n");
        return false;
    }
}

static void usage(char* progname) {
    printf(
            "\nUsage:\n"
            "%s <options>\n"
            "where options are:\n"
            "\t-w write: test writing to file\n"
            "\t-m memory: test malloc leaks\n"
            "\t-c memory: test calloc leaks\n"
            "\t-r memory: test realloc leaks\n"
            "\t-s test sync\n"
            "\t-S test trivial sync\n"
            "\t-p test PI calculation\n"
            "\t--cycles <N> perform N cycles\n"
            "\t--sleep <N> sleep N seconds before each cycle\n"
            "\t--isleep <N> sleep N seconds once before start\n"
            ,progname
    );
}

int main(int argc, char** argv) {

    if (!process_flags(argc, argv)) {
        usage(argv[0]);
        return 1;
    }

    if (initial_sleep > 0) {
        printf("Sleeping %d sec...\n", initial_sleep);
        sleep(initial_sleep);
        printf("Awoke\n");
    }
    
    if (test_sync) {
        test_sync_init();
    }

    int i;
    for (i = 0; i < cycles; i++) {
        if (i > 0 && step_sleep > 0) {
            printf("\tsleeping %d sec...\n", step_sleep);
            sleep(4);
        }
        printf("cycle %d\n", i);
        test(i);
    }

    if (test_sync) {
        test_wait_shutdown();
    }

    return 0;
}
