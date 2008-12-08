
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

template <class T1, class T2, class T3> struct C {
    typedef T1 t1;
    typedef T3 t3;
};

struct S1 {
    typedef int i;
};

struct S3 {
    typedef int k;
};

int main2() {
    C<S1, S2, S3>::t1::i a;
    C<S1, S2, S3>::t3::k b;
}
