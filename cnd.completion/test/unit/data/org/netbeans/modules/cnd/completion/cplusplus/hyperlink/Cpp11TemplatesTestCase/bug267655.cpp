namespace bug267655 {

    template <typename...>
    struct void_t267655 {
        typedef void type;
    };

    /// Implementation of the detection idiom (negative case).
    template<typename _AlwaysVoid, template<typename> class _Op, typename _Arg>
    struct __detector267655 {
        using type = void;
    };

    /// Implementation of the detection idiom (positive case).
    template<template<typename> class _Op,
    typename _Arg>
    struct __detector267655<typename void_t267655<_Op<_Arg>>::type, _Op, _Arg>
    {
        using type = _Op<_Arg>;
    };

    template<typename _Tp>
    using __rebind267655 = _Tp;

    struct MyCls267655 {
        void foo();
    };

    template <typename T>
    struct myalloc267655 {
        typedef T value_type;

        template <typename _Tp>
        struct rebind {
            typedef myalloc267655<_Tp> other;
        };
    };

    void zoo267655() {
        __detector267655<void, __rebind267655, MyCls267655>::type var;
        var.foo();
    }
}
