namespace bug238688 {
    struct AAA238688 {
        int foo();
    };

    struct BBB238688 {
        int boo();
    };

    AAA238688 operator+(const AAA238688 &a, const BBB238688 &b) {
        return a;
    }

    BBB238688 operator+(const BBB238688 &b, const AAA238688 &a) {
        return b;
    }

    int function238688() {
        AAA238688 a;
        BBB238688 b;
        auto var1 = a + b;
        var1.foo();
        auto var2 = b + a;
        var2.boo();
    } 
}