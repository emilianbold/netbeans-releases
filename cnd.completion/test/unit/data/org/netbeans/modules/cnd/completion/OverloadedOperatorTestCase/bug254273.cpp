namespace bug254273 {
    struct AAA254273 {
        void foo();
    };

    struct BBB254273 {
        typedef AAA254273* pointer;
        pointer operator->();
        void boo();
    };

    struct CCC254273 {
        typedef BBB254273* pointer;
        pointer operator->();
    };

    void roo254273() {
        CCC254273 ccc;
        // code is inserted here
    }
}