namespace {
    struct AAA {
        int foo();    
        AAA operator>>(int v);
    };

    struct BBB : AAA {
        int boo();
    };

    typedef BBB CCC;

    int zoo() {
        CCC bb;
        (bb >> 1).foo();
    }
}