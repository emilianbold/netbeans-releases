
class B : A {
    class D {
        
    };
};

namespace N {
    class A : B {

    };
}

class A : B {
    void foo();
};

void A::foo() {
    C a;
}
