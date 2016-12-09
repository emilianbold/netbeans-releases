namespace bug269292 {
    struct AAA269292 {
        int foo();
    };

    struct BBB269292 : AAA269292 {
        int boo();
    };

    decltype(auto) var1_269292 = AAA269292();

    decltype(auto) func269292() {
        return BBB269292();
    }

    int main269292() {
        var1_269292.foo();
        func269292().foo();
        func269292().boo();
        decltype(auto) var2 = BBB269292();
        var2.boo();
        return 0; 
    }  
}