class MyClass {
public:
    int public_field_1;
    int public_method_1();
protected:
};

void foo(MyClass c) {
    c.public_field_1 = 0;
    c.public_method_1();
}

void boo() {
    MyClass clazz;
    foo(cla);
}