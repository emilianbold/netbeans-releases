class AAA {
public:
    void foo();
};

class BBB : private AAA {
public:
    AAA::foo;
};

void accessFoo() {
    BBB b;
    b.foo();
}
