
struct bug180828_a {
    template <typename T> void f() {};
    int k;
};

template <> void bug180828_a::f<int>() { ++k; }
