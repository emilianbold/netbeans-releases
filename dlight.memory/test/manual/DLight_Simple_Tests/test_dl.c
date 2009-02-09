#include "test_dl.h"

#include <stdio.h>
#include <dlfcn.h>
#ifndef __APPLE__
#include <link.h>
#endif

#ifdef __APPLE__
#include <malloc/malloc.h>
#else
#include <malloc.h>
#endif


void test_dl(int step) {
    Dl_info di;
    dladdr(free, &di);
    printf("di.dli_fname %s\n", di.dli_fname);
    printf("di.dli_sname %s\n", di.dli_sname);
}
