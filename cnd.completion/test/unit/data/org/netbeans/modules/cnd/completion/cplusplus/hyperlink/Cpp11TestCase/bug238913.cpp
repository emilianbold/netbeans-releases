namespace bug238913 {
    
    struct AAA_238913 {
        int foo();
    };

    typedef AAA_238913 Typedef_238913(int a);
    
    template <typename A>
    using Alias_238913 = A (*)(int a);

    template <typename T>
    struct Cls_238913 {
        T (*fun)(int a);
        typedef T Fun(int b);
    };

    int boo_238913() {
      Typedef_238913 a;
      a(0).foo();        
        
      Cls_238913<AAA_238913> b; 
      b.fun(0).foo();

      Cls_238913<AAA_238913>::Fun c;
      c(0).foo(); 

      Alias_238913<AAA_238913> d;
      d(0).foo();
    }   
    
    struct XXX_238913 {
      auto (*fun)(int a) -> decltype(a + a);
    };
}