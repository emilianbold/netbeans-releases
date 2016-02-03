namespace bug216095 {
    namespace blabla216095 {
        typedef int mytype216095;
    }

    template <typename T = typename blabla216095::mytype216095>
    struct AAA216095 {
        // nothing here  
    }; 

    template <>
    struct AAA216095<int> {
        void foo();
    };

    void boo216095() {
        AAA216095<> var;
        var.foo();
    }
}