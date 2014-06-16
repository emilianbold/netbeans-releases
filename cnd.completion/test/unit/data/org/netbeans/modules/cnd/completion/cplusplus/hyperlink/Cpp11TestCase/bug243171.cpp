namespace bug243171 {
    struct MyStruct_243171 {
        int foo();
    };

    template <typename T>
    struct AAA1_243171 {
        T operator->();
    };

    template <typename T>
    using Alias1_243171 = AAA1_243171<T>;

    namespace NNN {
        template <typename T>
        struct AAA2_243171 {
            T operator->();
        };

        template <typename T>
        using Alias2_243171 = AAA2_243171<T>;    
    }

    int test_243171() {
        auto var1 = AAA1_243171<MyStruct_243171>();
        var1->foo();

        auto var2 = Alias1_243171<MyStruct_243171>();
        var2->foo();

        auto var3 = NNN::AAA2_243171<MyStruct_243171>();
        var3->foo();

        auto var4 = NNN::Alias2_243171<MyStruct_243171>();
        var4->foo();    
        
        auto var5 = new MyStruct_243171();
        var5->foo();
    }
}