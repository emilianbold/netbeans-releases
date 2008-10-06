namespace IZ140795 {

    template<typename T> struct TypeTraits {
        enum { value = 1 };
    };

    void test() {
        !TypeTraits<int>::value;
        TypeTraits<int *>::value;
        TypeTraits<int &>::value;
        TypeTraits<const int>::value;
        TypeTraits<const int &>::value;
        TypeTraits<volatile int>::value;
        TypeTraits<const volatile int>::value;
    }

}
