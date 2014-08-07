namespace bug246332_1 {
    ////////////////////////////////////////////////////////////////////////////////
    // Expression parameter passing    
    
    template <bool val>
    struct MyStruct246332_1 {
        int boo(); 
    };  

    template <>
    struct MyStruct246332_1<true> {
        int foo();
    };

    template <bool val1, bool val2>
    struct AAA246332_1 {};

    template <bool val1>
    struct AAA246332_1<val1, true> {
        typedef MyStruct246332_1<val1> type;
    };

    int main246332_1() {
        AAA246332_1<true, true>::type var1;
        AAA246332_1<false, true>::type var2; 
        var1.foo();
        var2.boo(); 
        return 0;
    } 

    ////////////////////////////////////////////////////////////////////////////////
    // Template parameter deducing

    struct ZZZ246332_1 {
        int foo();
    };

    template <typename T>
    struct YYY246332_1 {};

    template <typename T>
    struct YYY246332_1<YYY246332_1<T> > {
        typedef T type;
    };

    typedef typename YYY246332_1<YYY246332_1<ZZZ246332_1> >::type alias246332_1;

    int boo246332_1() {
        YYY246332_1<YYY246332_1<ZZZ246332_1> >::type var1;
        alias246332_1 var2;
        var1.foo();
        var2.foo();
    }  
}