
template<
    typename IsLE, 
    typename Tag, 
    template< typename P1 > class F, 
    typename L1
>
struct le_result1 {
    typedef F<
            typename L1::type
            > result_;
    typedef result_ type;
    F method();
    F field;
};