template <class T> class bug195283_A {
};

template <> class bug195283_A<long double> {
};

bug195283_A<long double> a;