template<typename _Tp> _Tp bug210192_declval();

template<typename _Tp, typename _Up>
struct bug210192_A {
    typedef __decltype(true ? bug210192_declval<_Tp > () : bug210192_declval<_Up > ()) typet;
};