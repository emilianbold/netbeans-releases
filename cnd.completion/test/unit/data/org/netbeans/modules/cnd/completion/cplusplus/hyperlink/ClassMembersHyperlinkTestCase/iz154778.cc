
// IZ#154778: Completion fails on gt operator

template <bool b> struct E {
    typedef int eType;
};

int main() {
    E<(1>2)>::eType k;
}
