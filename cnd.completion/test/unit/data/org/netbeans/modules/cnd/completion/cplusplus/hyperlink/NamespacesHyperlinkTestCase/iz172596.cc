namespace iz172596_A {
    namespace B {
        namespace C {
            namespace {
                namespace {
                    static const int XXXX = 200;
                }
            }

            class Class {
            public:

                Class() {
                }

                void method() {
                    int chunk = XXXX;
                }
            };
        }
    }
}

namespace {
    namespace {
        static const int iz172596_XXXX = 200;
    }
}

class iz172596_Class {
public:

    Class() {
    }

    void method() {
        int chunk = iz172596_XXXX;
    }
};

int iz172596_main(int argc, char** argv) {
    iz172596_A::B::C::Class c;
    c.method();
    iz172596_Class c2;
    c2.method();
    return 0;
}
