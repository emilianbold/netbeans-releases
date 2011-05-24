template <int i> class A: public A<i*2> {
};

template <> struct A<16> {
    int i;
};

int main() {
    A<2> a;
    a.i++; // unresolved
}