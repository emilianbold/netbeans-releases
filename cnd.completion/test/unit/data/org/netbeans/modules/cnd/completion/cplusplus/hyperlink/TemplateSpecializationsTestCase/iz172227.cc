template <class T> struct iz172227_A {
    int i;
};
template <> struct iz172227_A<int> {
    int j;
};

template <class T, class T2 = int> struct iz172227_B {
    iz172227_A<T2> a;
};

void foo() {
    iz172227_B<char, char> b;
    b.a.i;
    iz172227_B<char> b2;
    b2.a.j;
}
