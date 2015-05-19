class MyClass;

int foo(const MyClass* p);


class MyClass {
public:
    int pub_method() const;
};

typedef MyClass* pMyClass;

int MyClass::pub_method() const {
    return 0;
}

class ClassExt : public MyClass {   
    void bbooo();
};

int foo(const MyClass* p) {
    return p->pub_method();
}

namespace test{
int foo() {
    MyClass c1;
    MyClass c2;
    return c1.pub_method() + c2.pub_method();
}

}
//#define Macro(X) 12
void ClassExt::bbooo() {
    MyClass c3;
    foo(&c3);
    test::foo();
//    int value = Macro(15);
}

//#define Macro(X) X

//int value = Macro(10);
