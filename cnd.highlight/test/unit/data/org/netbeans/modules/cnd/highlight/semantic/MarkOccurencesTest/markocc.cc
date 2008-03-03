#define MOO 3

class Foo {
    int boo;
public:
    Foo();
    Foo(int _boo);

    void doFoo(int moo);
};

Foo::Foo(): boo(0) {
}

Foo::Foo(int _boo) {
    boo = _boo;
}

void Foo::doFoo(int moo) {
    int goo = MOO;
    boo = moo + goo;
}


