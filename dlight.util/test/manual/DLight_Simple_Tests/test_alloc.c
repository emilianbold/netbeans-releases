#include <stdio.h>
#include <stdlib.h>

#ifdef __APPLE__
#include <malloc/malloc.h>
#else
#include <malloc.h>
#endif

#include <limits.h>

static int total_leak = 0;

static void print_mstats() {
#ifdef __APPLE__
    struct mstats ms = mstats();
    printf( "Malloc statistics:\n"
            "\tms.bytes_free=%d\n"
            "\tms.bytes_total=%d\n"
            "\tms.bytes_used=%d\n"""
            "\tms.chunks_used=%d\n"
            "\tms.chunks_free=%d\n",
            ms.bytes_free, ms.bytes_total, ms.bytes_used, ms.chunks_used, ms.chunks_free);
#else
        struct mallinfo mi = mallinfo();
        printf( "Malloc statistics:\n"
                "\tarena %d\n"
                "\tordblks %d\n"
                "\tsmblks %d\n"
                "\thblks %d\n"
                "\thblkhd %d\n"
                "\tusmblks %d\n"
                "\tfsmblks %d\n"
                "\tuordblks %d\n"
                "\tfordblks %d\n"
                "\tkeepcost %d\n",
                mi.arena,
                mi.ordblks,
                mi.smblks,
                mi.hblks,
                mi.hblkhd,
                mi.usmblks,
                mi.fsmblks,
                mi.uordblks,
                mi.fordblks,
                mi.keepcost
                );

#endif
}

void leak_malloc(int size, int step) {
    if (step%4==0) {
        unsigned int hudge = (unsigned int) INT_MAX -1 + INT_MAX;
        void *p = malloc(hudge);
        printf(">> leak_malloc size=%X returned %X\n", hudge, p);
        return;
    }
    printf(">> leak_malloc size=%d\n", size);
    void* p1 = malloc(size/2);
    void* p2 = malloc(size);
    void* p3 = malloc(size*2);
    free(p1);
    // forgot to free p2
    free(p3);
    total_leak += size;
    printf("<< leak_malloc total_leak=%d\n", total_leak);
    print_mstats();
}

void leak_calloc(int size, int step) {
    printf(">> leak_calloc size=%d\n", size);
    int el_size = 10;
    size /= el_size;
    void* p1 = calloc(size/2, el_size);
    void* p2 = calloc(size, el_size);
    void* p3 = calloc(size*2, el_size);
    free(p1);
    // forgot to free p2
    free(p3);
    int leak = size * el_size;
    total_leak += leak;
    printf("<< leak_calloc leak=%d total_leak=%d\n", leak, total_leak);
    print_mstats();
}

void leak_realloc(int size, int step) {
    printf(">> leak_realloc size=%d\n", size);
    int leak = 0;

    // realloc to the same size
    void* p1 = malloc(size/2);
    p1 = realloc(p1, size/2);
    leak += size/2;

    // realloc to smaller size
    void* p2 = malloc(size/2);
    p2 = realloc(p2, size/4);
    leak += size/4;

    // realloc to bugger size
    void* p3 = malloc(size/8);
    p3 = realloc(p2, size/4);
    leak += size/4;

    total_leak += leak;
    printf("<< leak_realloc leak=%d total_leak=%d\n", leak, total_leak);
    print_mstats();
}
