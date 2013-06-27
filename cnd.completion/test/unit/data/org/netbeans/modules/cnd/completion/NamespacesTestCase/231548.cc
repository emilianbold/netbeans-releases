namespace {namespace std {
    class String{
    public:
        String(char *) {
            
        }
        int length() {return 0;}
    };
}
namespace my_namespace {
  namespace std_alias = std;
  class A {
  public:
      class B {
          
      };
  };
}

int main() {
    using namespace my_namespace;
    std_alias::String s("hello");
    
    A::B b;
    return 0;
}}