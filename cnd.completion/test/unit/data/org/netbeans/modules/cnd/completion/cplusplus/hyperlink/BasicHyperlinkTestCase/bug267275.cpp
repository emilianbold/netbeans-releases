namespace bug267275 {
    struct AAA267275  {
        int foo();
    };

    void boo267275() {
        void *ptr = new AAA267275();
        bool val = true;
        int x = val ? ((AAA267275*) ptr)->foo() : 10;
    } 
}