namespace {
  using A = struct AA {
      int x;
  };

  using B = enum BB {
      Y
  };

  using C = struct CC {
      int foo() {
          return 0;
      }
  };


  int foo() {
      A a;    
      a.x = Y;

      B b;
      b = Y;

      return C().foo() + CC().foo();
  }  
}