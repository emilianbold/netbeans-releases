namespace bug235120_2 {
    struct Foo_235120_2 {
        int aaa;
    };
    
    struct FooPtr_235120_2 {
        int bbb;
    };
    
    struct Boo_235120_2 {
        int ccc;
    };
    
    template<typename _Tp>
    struct remove_reference_235120_2
    { typedef _Tp   type; };

    template<typename _Tp>
    struct remove_reference_235120_2<_Tp&>
    { typedef _Tp   type; };    
    
    template <typename T>
    struct delete_default_235120_2 {};
    
    template <>
    struct delete_default_235120_2<Foo_235120_2> {
        typedef FooPtr_235120_2* pointer;
    };
    
    template <typename _Tp, typename _Dp = delete_default_235120_2<_Tp>>
    class UniquePointer_235120_2
    {
      class _Pointer
      {
        template<typename _Up>
        static typename _Up::pointer __test(typename _Up::pointer*);

        template<typename _Up>
        static _Tp* __test(...);

        typedef typename remove_reference_235120_2<_Dp>::type _Del;

      public:
        typedef decltype(__test<_Del>(0)) type;
      };
      
    public:      
      typedef typename _Pointer::type pointer;
    };       
    
    int test_235120_2() {
        UniquePointer_235120_2<Foo_235120_2>::pointer uptr1;
        uptr1->bbb;
        UniquePointer_235120_2<Boo_235120_2>::pointer uptr2;
        uptr2->ccc;
    }
}      