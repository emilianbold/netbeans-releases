
#include "file.h"

static int foo() {
    struct A* s;
    void *v;
    s = new A();
    v = s;
    ((struct A *) v)->f2(); // <-- code completion listbox wrong
    static_cast<struct A *> (v)->f2(); // <-- code completion listbox wrong
    return 0;
}

static int foo2() {
    class A* c;
    void *v;
    c = new A();
    v = c;
    ((class A *)v)->f2(); // <-- code completion listbox wrong
    static_cast<class A *> (v)->f2(); // <-- code completion listbox wrong
    return 0;
}
