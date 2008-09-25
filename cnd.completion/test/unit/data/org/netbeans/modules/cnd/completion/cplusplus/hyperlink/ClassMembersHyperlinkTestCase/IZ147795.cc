namespace IZ147795 {

    class MyClass {
    public:
        MyClass* myMethod() {}
    };

    int main(int argc, char** argv) {
        MyClass c;
        c.myMethod();
        (&c)->myMethod();
        (c).myMethod();
        (c.myMethod())->myMethod();
        return 0;
    }

}
