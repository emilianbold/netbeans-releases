namespace bug246548_1 {
    template <typename...Elems>
    struct AAA246548_1;

    template <typename Head, typename...Elems>
    struct AAA246548_1<Head, Elems...> : AAA246548_1<Elems...> {
        typedef int inner;
    };

    template <>
    struct AAA246548_1<> {
        typedef int stop;
    };

    int main246548_1() {
        AAA246548_1<int, double>::inner var1;
        AAA246548_1<int, double>::stop var2;
        return 0;
    }  
}