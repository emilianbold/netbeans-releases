namespace bug267502 {
    struct AAA267502 {
        void foo();
    };

    template <typename T>
    struct BBB267502 {};

    template <typename T>
    struct BBB267502<BBB267502<T>> {
        typedef T template_type;
    };

    template <typename T>
    struct CCC267502 {
        typedef BBB267502<T> orig_type;
        typedef CCC267502<T>::orig_type nested_type;
    };

    typedef CCC267502<AAA267502>::nested_type type267502;

    void boo267502() {
        BBB267502<type267502>::template_type var;
        var.foo();
    } 
}