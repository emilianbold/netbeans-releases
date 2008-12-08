
// IZ#154775: Unresolved inner type of instantiation

template <class T> struct B {
    typedef T bType;
};

struct A {
    typedef int aType;
    void foo();
};

int main() {
    B<A>::bType::aType i;
}