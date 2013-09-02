namespace bug235399 {
  struct AAA {
      int foo();
  };

  struct BBB {
      int boo();
  };

  template <typename T1, typename T2>
  T1 roo(T1 t1, T2 t2);

  template <typename T1, typename T2>
  T2 soo(T1 t1, T2 t2);

  int zoo() {
      AAA a;
      BBB b;
      roo(a, b).foo();
      roo<AAA>(a, b).foo();
      roo<AAA, BBB>(a, b).foo();
      soo(a, b).boo();
      soo<AAA>(a, b).boo();
      soo<AAA, BBB>(a, b).boo();
  } 
}