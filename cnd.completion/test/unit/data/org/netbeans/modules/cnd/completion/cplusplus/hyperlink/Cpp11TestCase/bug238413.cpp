namespace bug238413 {
  
  namespace AAA {
      inline namespace BBB {
          inline namespace CCC {
              int foo();
          }
      }
  }

  int boo() {
      AAA::BBB::CCC::foo();
      AAA::BBB::foo();
      AAA::foo();
      return 0;
  }

  namespace a { 
      inline namespace b { 
          struct foo { 
              static int bar() { 
                  return 0; 
              } 
          }; 
      } 
  } 

  int main() { 
      return a::foo::bar(); 
  }  
  
}