namespace bug234973 {
    typedef int T1_234973;

    typedef T1_234973 T2_234973;

    typedef T2_234973 T3_234973;

    template <typename T>
    struct AAA_234973 {
    };

    template <>
    struct AAA_234973<int> {
        int y;
    };
    
    template <typename T>
    struct BBB_234973 {
    };

    template <>
    struct BBB_234973<T2_234973> {
        int y;
    };    

    int foo_234973() {    
        AAA_234973<T3_234973> a;
        a.y = 0; // y is unresolved
        
        BBB_234973<T3_234973> b;
        b.y = 0; // y is unresolved        
    }
}