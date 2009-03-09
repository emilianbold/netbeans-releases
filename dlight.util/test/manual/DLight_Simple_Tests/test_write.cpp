
#include "test_write.h"

#include <stdio.h>
#include <string.h>

extern "C" void test_write(int mb) {
    char* fname = tmpnam(0);
    printf("writing %d Mbytes to temp file %s\n", mb, fname);
    FILE* file = fopen(fname, "w");

    char* text = "a quick brown fox jumps over the lazy dog\n";
    int textlen = strlen(text);
    int cnt = mb*1024*1024 / textlen;

    for( int i = 0; i < cnt; i++) {
        fwrite(text, textlen, 1, file);
    }
    printf("writing done. removing %s\n", fname);
    remove(fname);
}
