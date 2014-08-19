namespace bug246548_2 {
    template <int Ind, typename...Elems>
    struct AAA246548_2;

    template <int Ind, typename Head, typename...Elems>
    struct AAA246548_2<Ind, Head, Elems...> : AAA246548_2<Ind + 1, Elems...> {
        typedef int inner;
    };

    template <int Ind>
    struct AAA246548_2<Ind> {
        typedef int stop;
    };

    int main246548_2() {
        AAA246548_2<0, int, double>::inner var1;
        AAA246548_2<0, int, double>::stop var2;
        return 0;
    } 
}