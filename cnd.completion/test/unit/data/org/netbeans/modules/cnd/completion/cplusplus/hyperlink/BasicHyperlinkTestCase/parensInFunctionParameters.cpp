namespace test_parens {
  
  int testParensFoo(int (a));

  int testParensBoo(void (fun1)(double (param)), void (*fun2)(void (*fun3)(double ((param)))));  
  
}