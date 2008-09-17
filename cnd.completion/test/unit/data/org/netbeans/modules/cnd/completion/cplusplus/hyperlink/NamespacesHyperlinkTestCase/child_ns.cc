namespace IZ145148 {
    namespace Y {
        struct Z {
            int zz;
        };
    }
    void do_z() {
        Y::Z z;
        z.zz = 1; // "zz" is unresolved
    }
}

namespace IZ145148_2 {
    namespace Y {
        struct Z { int z; };
        int foo() {
            Y::Z zz;
            zz.z; // "z" is not resolved
        }
    }
}

// IZ 145142 : unable to resolve declaration imported from child namespace
namespace IZ145142_B {
    int b = 0;
}

namespace IZ145142_A {
    using namespace IZ145142_B;
    int a = 0;
}

namespace IZ145142_B {
    using namespace IZ145142_A;
}

void IZ145142_foo() {
    IZ145142_A::a++;
    IZ145142_B::a++;
    IZ145142_A::b++;
    IZ145142_B::b++;
}

namespace IZ145142_B_2 {
    int i = 0;
}

namespace IZ145142_A_2 {
    namespace IZ145142_B_2 {
        typedef int S;
    }
    using IZ145142_B_2::S;

}
typedef IZ145142_A_2::S IZ145142_T;
