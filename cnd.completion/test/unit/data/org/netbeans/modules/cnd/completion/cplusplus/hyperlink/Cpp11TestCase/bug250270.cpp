namespace bug250270 {
  struct AAA250270 {
    int foo();
  };
  int main250270() {    
      using BBB250270 = AAA250270;
      BBB250270 var;
      var.foo();
      return 0;
  }    
}