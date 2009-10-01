

namespace {

template <class T>
struct A {

    T* doNothing() {
    }
};

class B {
    A<int> i;

    doSomething() {
        i.doNothing();
    }
};

}
