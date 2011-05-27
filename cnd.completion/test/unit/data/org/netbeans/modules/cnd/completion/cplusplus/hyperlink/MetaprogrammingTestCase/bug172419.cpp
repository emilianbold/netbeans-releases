namespace bug172419 {

template< bool C_ > struct bool_ {
    static const bool value = C_;
};

// shorcuts
typedef bool_ < true > true_;
typedef bool_ < false > false_;

template<
bool C
, typename T1
, typename T2
>
struct if_c {
    typedef T1 type;
};

template<
typename T1
, typename T2
>
struct if_c < false, T1, T2> {
    typedef T2 type;
};

template<
typename T1
        , typename T2
        , typename T3
        >
        struct if_ {
    typedef if_c<
            static_cast<bool> (T1::value), T2
            , T3
            > z;
    typedef typename z::type type;
};

struct A {
    void foo() {
    }
};

struct B {
};

int main() {
    
    typedef if_<false_, B, A > tt;
    tt::type a;
    a.foo();

    if_c<
            static_cast<bool> (false_::value), int
            , A
            > ::type a2;    
    a2.foo();
    
    return 0;
}

}
