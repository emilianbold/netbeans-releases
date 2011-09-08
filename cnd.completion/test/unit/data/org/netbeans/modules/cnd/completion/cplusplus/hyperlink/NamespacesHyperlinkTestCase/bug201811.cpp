struct bug201811_A {
    int n;
    struct B {
        int m;
    };
};

namespace bug201811_std1 {
    namespace tr2 {
        using ::bug201811_A;
        bug201811_A a1;
        namespace tr1 {
            using tr2::bug201811_A;
        }
    }
}

int bug201811_main() {
//    bug201811_std1::tr2::tr1::A x;
//    int n = x->n;
    bug201811_std1::tr2::bug201811_A x2;
    bug201811_std1::tr2::bug201811_A::B x3;
    bug201811_std1::tr2::tr1::bug201811_A::B x4;
    x2.n;
    x3.m;
    x4.m;

    return 0;
}
