struct A {
    int i;
};

template<typename T>
struct B {
    typedef T type;
};

template<typename T, typename Base = typename B<T>::type >
struct C : public Base {
};

int main() {
    C<A> c;
    c.i++; // unresolved
}
