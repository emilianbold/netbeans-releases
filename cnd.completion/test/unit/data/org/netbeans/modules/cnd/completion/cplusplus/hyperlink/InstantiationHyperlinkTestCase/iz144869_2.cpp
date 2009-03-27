template <class T1, class T2> class C {
};

template <class T> class C<T, int>{
public:
    int i;
};

template <class T> class C<int, T>{
public:
    int j;
};

template <class T1, class T2> class C<T1, T2* >{
public:
    int k;
};

template <class X> class ZZ {
public:
    X foo();
};

template <class X> class ZZ<X*> {
public:
    void boo();
};


int main() {
    C<char, int> c;
    c.i; // unresolved

    C<int, char> c2;
    c2.j; // unresolved

    C<bool, bool*> c3;
    c3.k; // unresolved

    ZZ<int *> t;
    t.boo();

    return 0;
}