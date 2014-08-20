namespace bug243083_1 {
    template <typename T>
    struct Recursion243083_1 {
        typedef Recursion243083_1<typename T::next> next;
        typedef T type;
    };
    
    struct Inner2_243083_1 {
        int foo2();
        typedef void next;
    };
    
    struct Inner1_243083_1 {
        int foo1();
        typedef Inner2_243083_1 next;
    };
    
    struct Inner0_243083_1 {
        int foo0();
        typedef Inner1_243083_1 next;
    };    
    
    void recurse243083_1() {
        Recursion243083_1<Inner0_243083_1>::next::next::type var2;
        var2.foo2();
        Recursion243083_1<Inner0_243083_1>::next::type var1;
        var1.foo1();        
        Recursion243083_1<Inner0_243083_1>::type var0;
        var0.foo0();            
    } 
}