namespace bug249463 { 
    struct AAA249463 {
        int foo() const;
    };

    AAA249463 someStr249463;

    namespace zoo249463 {
        auto &test2 = someStr249463;
    }
    
    struct roo249463 {
        static constexpr auto s_field = AAA249463();
    };
    
    void someFunc249463() {
      zoo249463::test2.foo();
      roo249463::s_field.foo();
    }
}