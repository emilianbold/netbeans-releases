namespace {
    namespace spec_quals_0 {
        struct MyStruct_spec_quals_0 {
            int foo();
        };

        template <typename T>
        struct AAA_spec_quals_0 {
            typedef T type;
        };

        template <typename T> 
        struct AAA_spec_quals_0<T*>{
            typedef T type;
        };

        int foo_spec_quals_0() {
            AAA_spec_quals_0<MyStruct_spec_quals_0*>::type var;
            ;
        } 
    }
}