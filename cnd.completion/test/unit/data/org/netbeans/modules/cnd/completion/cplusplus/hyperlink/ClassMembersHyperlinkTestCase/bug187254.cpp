namespace bug187254_C {
    struct A {
        virtual void foo() {};
        virtual ~A() {};
    };

    struct B : A {
        virtual void foo() {};
        virtual ~B() {};
    };
}

int bug187254_foo(bug187254_C::B* b){
   b->bug187254_C::B::foo(); // unresolved C
   b->bug187254_C::B::~B(); // unresolved C & ~B
   return 0;
}