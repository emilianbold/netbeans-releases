namespace bug246803 {
    template <typename = struct DDD246803>
    struct AAA246803 {
        int foo();
    };

    template <>
    struct AAA246803<DDD246803> {
        int boo();
    };

    void mainAAA246803() {
        AAA246803<> var;
        var.boo();
    }

    template <bool, typename = int>
    struct BBB246803 {
        int foo();
    };

    template <typename T>
    struct BBB246803<false, T> {
        int boo();
    };

    template <>
    struct BBB246803<true, int> {
        int roo();
    };

    template <>
    struct BBB246803<false, int> {
        int doo();
    };

    void mainBBB246803() {
        BBB246803<true> var1;
        var1.roo();
        BBB246803<false, int> var2;
        var2.doo();
        BBB246803<false, double> var3;
        var3.boo();
    }

    template <typename...>
    struct CCC246803 {
        int foo();
    };

    template <typename T>
    struct CCC246803<T> {
        int boo();
    };

    void mainCCC246803() {
        CCC246803<> var1;
        var1.foo();
        CCC246803<int> var2;
        var2.boo();
    }
}