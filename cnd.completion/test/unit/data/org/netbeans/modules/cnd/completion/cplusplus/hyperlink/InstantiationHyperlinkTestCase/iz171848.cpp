struct iz171848_A {
    int i;
};

template <class T, class TT = iz171848_A> struct iz171848_B;

template <class T, class TT> struct iz171848_B {
    TT t;
};

int iz171848_main(int argc, char** argv) {
    iz171848_B<int> b;
    b.t.i++; // unresolved i
    return (0);
}

namespace iz171848_N {
    struct iz171848_A2 {
        int i;
    };

    template <class T, class TT = iz171848_A2> struct iz171848_B2;

    template <class T, class TT> struct iz171848_B2 {
        TT t;
    };

    int iz171848_main(int argc, char** argv) {
        iz171848_B2<int> b;
        b.t.i++; // unresolved i
        return (0);
    }
}