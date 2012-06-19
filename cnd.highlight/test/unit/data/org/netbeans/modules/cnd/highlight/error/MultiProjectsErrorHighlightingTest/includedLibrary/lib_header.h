
#ifndef LIB_HEADER_202433_H
#define LIB_HEADER_202433_H

#include "other_lib_header.h"

struct EMPTY_MACRO_FROM_OTHER_INCLUDE Struct202433* pGlobal202433;

#ifdef MY_PLUS_PLUS
struct MyClass202433_One {
    void foo();
};
#else
struct MyClass202433_Two {
    void boo();
};
#endif

#endif

