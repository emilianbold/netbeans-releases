namespace iz159243_N1 {
    namespace iz159243_N2 {
        void iz159243_foo() {
        }
    }
    namespace iz159243_N2A = iz159243_N2;
}
using namespace iz159243_N1::iz159243_N2A;
int iz159243_main() {
    iz159243_foo();
    return 0;
}

namespace iz159243_N0 {
    namespace iz159243_N1 {
        namespace iz159243_N2 {
            void iz159243_foo2() {
            }
        }
        namespace iz159243_N2A = iz159243_N2;
    }
}
using namespace iz159243_N0::iz159243_N1::iz159243_N2A;
int iz159243_main2() {
    iz159243_foo2();
    return 0;
}