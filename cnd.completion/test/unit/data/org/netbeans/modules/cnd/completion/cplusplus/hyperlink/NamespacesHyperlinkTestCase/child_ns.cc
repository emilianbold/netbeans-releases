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
