struct bug199079_Z {
    int i;
};

template <class T> struct bug199079_B {
};

template <> struct bug199079_B<int> {
    typedef bug199079_Z btype;
};

template <class T> struct bug199079_A {    
    typedef bug199079_B<T> bt;
    typedef typename bt::btype atype;
};

template <class T> struct bug199079_AA {    
    typedef bug199079_A<T> at;
    typedef typename at::atype aatype;    
};

int main(int argc, char** argv) {
    bug199079_AA<int>::aatype a;
    a.i++; // unresolved i

    return 0;
}