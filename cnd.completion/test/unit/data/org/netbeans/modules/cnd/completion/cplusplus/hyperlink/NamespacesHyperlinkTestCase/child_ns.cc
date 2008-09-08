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
