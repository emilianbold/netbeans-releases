struct iz169305_A {
    int i;
    void foo() {
    }
};

struct iz169305_B {
    iz169305_A operator()(int i) {
        iz169305_A a;
        a.i = 1;
        return a;
    }
};

struct iz169305_C {
    iz169305_B operator()(int i) {
        iz169305_B b;
        return b;
    }
};

int iz169305_main() {
    iz169305_C c;
    c(1)(2).foo(); // unresolved foo
    return (0);
}
