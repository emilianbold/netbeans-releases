class A {
public:
    void foo();
};

template <T> class Map {
public:
    typedef T iterator;
};

typedef Map<A> mapA;
mapA::iterator itA; // try something like Map<A>::

void testIteratorVisibility() {
    Map<A>::iterator itAA; // try something like Map<A>::
    itAA.foo();
    itA.foo();
}