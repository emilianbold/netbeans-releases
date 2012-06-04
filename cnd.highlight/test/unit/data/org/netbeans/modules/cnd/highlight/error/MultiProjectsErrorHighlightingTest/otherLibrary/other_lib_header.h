
#ifndef OTHER_LIB_HEADER_202433_H
#define OTHER_LIB_HEADER_202433_H

#define EMPTY_MACRO_FROM_OTHER_INCLUDE

struct Struct202433 {
    int field1;
    int field2;
};

namespace ns210384 {
    void foo210384() {
    }
}

#ifdef MY_PLUS_PLUS
struct MyClass202433_Other {
    void foo2();
};
#else
struct MyClass202433_Yet_Other {
    void boo2();
};
#endif

#endif
