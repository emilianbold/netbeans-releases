namespace bug238847_2 {

    struct MyClass_238847_2 {
        void foo() {}
    };

    MyClass_238847_2 global;

    #define INTRODUCE_TYPE_238847_2(NAME) typedef decltype(global) NAME

    INTRODUCE_TYPE_238847_2(mytype_238847_2);

    int main()
    {  
        mytype_238847_2 xxx;
        xxx.foo();
    }  
        
}