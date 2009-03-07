template <class T> struct iz159068_A {
    int i;
    void foo() {
        &iz159068_A<int>::i; // unresolved
    }
};

int iz159068_main() {
    iz159068_A<int> a;
    a.foo();
    return 0;
}