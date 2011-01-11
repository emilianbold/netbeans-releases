template <typename TYPE> class bug187258_ACE_Atomic_Op {
};

template<> class bug187258_ACE_Atomic_Op<long> {
public:
    static void init_functions(void) {

    }

};

template<> class bug187258_ACE_Atomic_Op<unsigned long> {
public:
    static void init_functions(void) {

    }

};

int foo() {
    bug187258_ACE_Atomic_Op<long>::init_functions();
    bug187258_ACE_Atomic_Op<unsigned long>::init_functions();
}
