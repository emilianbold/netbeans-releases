
namespace bug172419_5 {
    struct AAA {
    };

    struct BBB {
        int bbb();
    };

    struct XXX {
    };

    struct YYY {
        int yyy();
    };

    struct void_ {};

    template <bool C, typename TT1, typename TT2>
    struct if_else {
        typedef TT2 type;
    };

    template <typename TT1, typename TT2>
    struct if_else<true, TT1, TT2> {
        typedef TT1 type;
    };

    template <typename X1, typename X2>
    struct is_same {
        static const bool value = false;
    };

    template <typename X>
    struct is_same<X, X> {
        static const bool value = true;
    };

    struct switch_plain {
        typedef typename 
        if_else< 
            is_same<XXX, AAA>::value, BBB, 
            typename if_else< is_same<XXX, XXX>::value, YYY, void_>::type 
        >::type type;
    };

    template <typename Selector, typename Case1, typename Value1, typename Case2, typename Value2>
    struct switch_complex {
        typedef typename 
        if_else< 
            is_same<Selector, Case1>::value, Value1, 
            typename if_else< is_same<Selector, Case2>::value, Value2, void_>::type 
        >::type type;
    };

    int foo() {
        switch_plain::type var1;
        var1.yyy();

        switch_complex<XXX, AAA, BBB, XXX, YYY>::type var2;
        var2.yyy();
    } 
}
  