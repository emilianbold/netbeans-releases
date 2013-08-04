namespace {
  
  struct AAA {
    int xx;
  };
  
  struct BBB {
    typedef AAA type;
  };
  
  BBB var;    

  void foo(const decltype(var)::type& it) { 
    it.xx;
  }
}