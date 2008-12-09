
// IZ#154781: Completion fails on const

template <class T> struct B {
    typedef T bType;
};

struct A {
    typedef int aType;
    void foo();
};

int main() {
    B<A const&>::bType m = A();
    B<A const>::bType o = A();
}
