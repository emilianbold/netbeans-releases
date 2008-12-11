
// IZ#154789: Completion fails on macros

template <class T> struct B {
    typedef T bType;
};

struct A {
    typedef int aType;
    void foo();
};

int main() {
#define TYPENAME typename
    B<TYPENAME A>::bType n;
}