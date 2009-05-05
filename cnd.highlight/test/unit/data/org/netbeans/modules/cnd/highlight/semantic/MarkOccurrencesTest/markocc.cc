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
    int bar = 1;
    ::bar = ::bar + bar + 1;
    func(::bar);
}

int bar = 1;
void func(int bar) {}

#ifdef MOO

#  elif BOO

#if BOO != 0

#endif

# else

#ifndef INTERNAL

# endif

#endif

namespace N1
{
    int fooN1(int par0 /* = 0 */); // no highlighting
    int fooN1(int par0 /* = 0 */);

    int fooN1(int par0 /* = 0 */) {

    }


    class AAA {
        void const_fun(int i) ;
        void const_fun(int i) const ;
    };


    void AAA::const_fun(int i) {

    }

    void AAA::const_fun(int i) const {

    }
}
