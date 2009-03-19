struct SmartPtr {
    struct Tester {
        Tester(int) {
        }
        void dummy() {
        }
    };

    void foo() {
        true ? 0 : &Tester::dummy;
    }
};