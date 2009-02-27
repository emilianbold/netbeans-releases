namespace N159223 {
    namespace N2 {
        struct AA {
            typedef int TTT;
            int i;
        };
    }
    using N2::AA;
}
namespace N159223 {
    struct B : public AA {
        TTT t;
    };
}

namespace N159308 {
    namespace N2 {
        struct AAA {
        };
    }
}
namespace N159308 {
    namespace N3 {
        using N2::AAA;
        void foo() {
            AAA a;
        }
    }
}
