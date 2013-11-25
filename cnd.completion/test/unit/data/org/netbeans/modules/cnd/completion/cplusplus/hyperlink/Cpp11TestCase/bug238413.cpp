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
 
  namespace FFF {
      inline namespace GGG {
          namespace EEE {
              struct RRR {
                  int foo();
              };
          }

          inline namespace EEE {
              void roo();
          }
      }

      int loo() {
          EEE::RRR c;
          c.foo();
          RRR c1;
          c1.foo();
          roo();
      }
  }

  int main1() {
      FFF::loo();
      return 0;
  }
}