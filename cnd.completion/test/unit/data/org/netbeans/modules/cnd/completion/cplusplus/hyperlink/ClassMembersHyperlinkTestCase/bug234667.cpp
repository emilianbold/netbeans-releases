namespace bug234667 {
    struct bug234667_AAA {
        struct bug234667_BBB {
            int bug234667_foo();
        };
    };


    void bug234667_boo() {
        bug234667_AAA::bug234667_BBB().bug234667_foo();
    }
}
