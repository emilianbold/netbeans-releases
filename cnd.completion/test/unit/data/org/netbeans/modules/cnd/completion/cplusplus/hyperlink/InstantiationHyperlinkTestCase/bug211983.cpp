template <int t> struct bug211983_A {
};

template <> struct bug211983_A<0> {
    int i;
};

void bug211983_foo() {
    bug211983_A<1-1U> a;
    a.i;
}