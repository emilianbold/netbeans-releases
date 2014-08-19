namespace bug246517 {
    template <typename T> 
    struct add_reference246517 {
        typedef T& type;
    };

    template <typename T> 
    struct add_reference246517<T&> {
        typedef T& type;
    };

    template <typename...Elements>
    struct tuple246517 {};

    template< int I, class T >
    struct tuple_element246517;

    // recursive case
    template< int __i, class Head, class... Tail >
    struct tuple_element246517<__i, tuple246517<Head, Tail...>>
        : tuple_element246517<__i-1, tuple246517<Tail...>> { };

    // base case
    template< class Head, class... Tail >
    struct tuple_element246517<0, tuple246517<Head, Tail...>> {
       typedef Head type;
    };

    template <int Ind, class... Elements>
    typename add_reference246517<
        typename tuple_element246517<Ind, tuple246517<Elements...>>::type
    >::type 
    get(tuple246517<Elements...> &tpl);

    struct AAA246517 {
        int aaa();
    }; 
    struct BBB246517 {
        int bbb();
    };
    struct CCC246517 {
        int ccc();
    };

    int foo246517() {
        tuple246517<AAA246517, BBB246517, CCC246517> tpl;
        get<0>(tpl).aaa();
        get<1>(tpl).bbb();
        get<2>(tpl).ccc();
    } 
}