template< bool C_ > struct bool_
{
    static const bool value = C_;
};

typedef bool_<true> _true;

template <bool i> class A {
};

template <> struct A<true> {
    int i;
};

int main() {
    A<_true::value> a;
    a.i++; // unresolved
}