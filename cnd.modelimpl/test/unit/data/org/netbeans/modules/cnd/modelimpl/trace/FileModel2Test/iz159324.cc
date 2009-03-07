namespace N {
    struct A {
        A() {
        }
        A(int i) {
        }
    };
}
void foo () {
    int i;
    N::A a1(i), a2;
    int i1(i), i2;
    int i3(1), i4;
}