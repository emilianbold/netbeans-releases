namespace {
    
    template <class T, bool >
    struct AAA {
        int aa;  
    };
    //
    template <class T>
    struct AAA<T, true> {
        int bb;
    };


    AAA<int, true> foo();

    int boo() {
        foo().bb; // bb is unresolved
    }    
    
}