
template <class T, class TT> class explicit_specializations_C {
public:
    T i;
    void foo();
};

template<class T, class TT> void explicit_specializations_C<T,TT>::foo() {
    i++;
}

template<class TT> class explicit_specializations_C<long,TT> {
    int i;
public:
    void foo();
};

template<class TT> void explicit_specializations_C<long,TT>::foo() {
    i++;
}

template<> void explicit_specializations_C<int,int>::foo();

template<> void explicit_specializations_C<int,int>::foo() {
    i++;
}

int explicit_specializations_main(int argc, char** argv) {

    explicit_specializations_C<int, int> c;
    c.foo();

    explicit_specializations_C<long, char> c2;
    c2.foo();

    explicit_specializations_C<char,bool> c3;
    c3.foo();

    return 0;
}