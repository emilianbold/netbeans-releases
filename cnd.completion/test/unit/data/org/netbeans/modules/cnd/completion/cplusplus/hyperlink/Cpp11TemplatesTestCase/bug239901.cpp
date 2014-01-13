namespace bug239901 {
    struct A239901 {
        int foo();
    };

    struct B239901 {
        int boo();
    };

    template <class T = A239901>
    struct XXX239901 {
        T foo();
    };

    template<class T = A239901> using Type239901 = T;

    void function239901() {
        Type239901<> t;
        t.foo();

        Type239901<B239901> t1;
        t1.boo();    

        XXX239901<> a;
        a.foo().foo(); 
    }  
}
