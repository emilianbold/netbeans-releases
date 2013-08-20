namespace {
    struct AAA {
        int xx;
    };

    struct BBB : decltype(AAA) {
        int yy;
    };

    int foo() {
        BBB b;
        b.xx;
    }
}
     