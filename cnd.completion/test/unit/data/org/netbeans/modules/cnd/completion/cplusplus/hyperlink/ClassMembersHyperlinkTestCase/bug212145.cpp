class bug212145_A {
protected:
    void foo() {        
    }
};

class bug212145_B : public bug212145_A::bug212145_A::bug212145_A::bug212145_A {
    void bar() {
        foo();
    }
};