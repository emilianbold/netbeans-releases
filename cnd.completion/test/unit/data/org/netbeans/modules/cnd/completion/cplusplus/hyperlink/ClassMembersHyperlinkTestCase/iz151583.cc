class iz151583_A {
public:
    class B;
    class C;
    typedef int *pint;
};

class iz151583_A::B {
    C c; // C unresolved
    pint i; // pint unresolved
};

class iz151583_A::C {
    B b; // B unresolved
    pint j; // pint unresolved
};