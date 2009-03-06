template<typename T>
struct iz159054_A {
    A<T>& foo();
    A<T>* foo2();
};

template<typename T>
iz159054_A<T>&
iz159054_A<T>::foo() { // unresolved
    return *this;
}

template<typename T>
iz159054_A<T>*
iz159054_A<T>::foo2() { // unresolved
    return *this;
}

int main() {
    iz159054_A<int> a;
    a.foo();
    a.foo2();

    return 0;
}