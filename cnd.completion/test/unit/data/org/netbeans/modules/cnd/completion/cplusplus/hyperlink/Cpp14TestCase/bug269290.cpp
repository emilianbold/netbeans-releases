namespace bug269290 {
    struct AAA269290 {
        void foo();
    };

    auto boo269290() {
        static AAA269290 var;
        if (true) {
            return (AAA269290*)0;
        } else {
            return &var;
        }
    }   

    struct BBB269290 {
        auto coo() {
            static AAA269290 var;
            if (true) {
                return &var;
            }
        }   
    };

    int main269290() {
        boo269290()->foo();
        BBB269290 var;
        var.coo()->foo();
        return 0;
    }  
}