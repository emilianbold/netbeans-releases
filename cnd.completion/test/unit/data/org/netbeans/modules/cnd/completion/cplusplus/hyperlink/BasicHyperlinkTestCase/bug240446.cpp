namespace bug240446 {
    
    struct AAA240446 {
        int foo();
    };    
    
    struct BBB240446 {
        BBB240446 operator()();

        bool boo();
    };
    
    BBB240446 clazz;

    struct Class240446 {
        AAA240446 clazz();

        void foo() {
            BBB240446 clazz;
            clazz().boo(); // non_null is unresolved
        }
        
        void boo() {
            clazz().foo(); // non_null is unresolved
        }
    };
}