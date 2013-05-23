namespace {

    struct MyClass {
        void Test() {}
    };

    template <class T>
    typename T foo() {

    }

    int main()
    {
        foo<MyClass*>().;
        return 0;
    }

}