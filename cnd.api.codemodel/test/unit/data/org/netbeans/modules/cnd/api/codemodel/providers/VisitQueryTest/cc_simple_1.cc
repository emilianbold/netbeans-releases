class MyClass {
public:
    isnt public_field_1;
    int public_method_1();
protected:
};

void foo(MyClass c) {
    c.public_field_1 = 0;
    c.public_method_1();
}
