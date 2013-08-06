namespace {
    struct AAA {
        int xx;
    };

    struct BBB {
        typedef AAA CCC;
        int yy;
    };

    BBB foo();

    int zoo() {

        BBB g;
        decltype(g) b;
        b.yy;    

        BBB f;
        decltype(f)::CCC a; 
        a.xx;

        decltype(foo())::CCC d;
        d.xx;

        return 0;
    }  
}