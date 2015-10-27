namespace bug255475 {
    typedef const int* check_type255475;

    template <typename T>
    struct type_traits255475 {
        typedef T was_not_pointer;
    };

    template <typename T>
    struct type_traits255475<T*> {
        typedef T was_pointer;
    };

    void foo255475() {
        type_traits255475<
                type_traits255475<check_type255475>::was_pointer
        >::was_not_pointer var = 0;
    }
}