template <class T> struct IZ154779_B {
    typedef T bType;
};

int IZ154779_main() {
    IZ154779_B<
#ifdef IZ154779_A
    int
#else
    long
#endif
    >::bType l; // unresolved bType
}