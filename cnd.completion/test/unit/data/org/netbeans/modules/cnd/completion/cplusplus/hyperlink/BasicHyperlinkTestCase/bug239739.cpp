namespace bug239739 {

    typedef struct AAA_239739 SimpleTypedef_239739, (*FunPtrTypedef_239739)();

    struct AAA_239739 {
        int foo();
    };

    int foo_239739() {
        SimpleTypedef_239739 a;
        FunPtrTypedef_239739 b;
        a.foo();
        b().foo();
    }                
    
}