int a = 1;
template<int &b = a> void f() {
        f<>();
}