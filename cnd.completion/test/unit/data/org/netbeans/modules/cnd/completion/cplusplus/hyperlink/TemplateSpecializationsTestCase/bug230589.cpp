namespace {

    struct valueholder {
        static const int value = 1;
    };

    template<int A>
    struct funcholder { 
        int foo();
    };

    template<>
    struct funcholder<2> { 
        int boo();
    };

    funcholder<1 + valueholder::value> myclass_cast() {
        return funcholder<2>();
    }

    int foo() {
        myclass_cast().boo(); // boo is unresolved
    } 
    
}
