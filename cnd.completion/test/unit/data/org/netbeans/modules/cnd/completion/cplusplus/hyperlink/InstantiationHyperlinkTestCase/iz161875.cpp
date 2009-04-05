template <class T> struct iz161875_AA;
template <> struct iz161875_AA<int> {
    int i;
};
int iz161875_foo() {
    iz161875_AA<X> a;
    a.i;
}