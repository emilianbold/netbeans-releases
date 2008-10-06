namespace iz_143285 {
    struct Outer {
        struct Inner {};
    };
    class User {
        struct Cls {
            struct Inner {
                Inner(int);
            };
            void foo_cls_1() {
                Inner *i = new Inner(10); // Inner should be resolved
            }
        };
        typedef Outer TypedefedOuter;
        TypedefedOuter::Inner inner1; // Inner should be resolved
        Cls::Inner inner2; // Inner should be resolved
    };
}
