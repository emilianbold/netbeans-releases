namespace iz147518_2 {
    template <class T1, class T2, bool b> class C {
    };

    template <class T1, class T2> class C<T1, T2, true > {
    public:
        int i;
    };

    template <class T1, class T2> class C<T1, T2, false > : public C<T1, T2, true > {
    public:
        using C<T1, T2, true > ::i; // unresolved

        int foo() {
            i++; // unresolved
        }
        static int z;
    };

    int main() {
        C<int, int, false> c;
        c.foo(); // unresolved
        C<int, int, false> c2;
        c2.i; // unresolved
        C<int, int, false>::z++; // unresolved
        return 0;
    }
}