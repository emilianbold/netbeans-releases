namespace util {
   class list {
       public:
       class iterator {
       public:
           void foo() {};
       };
   };
}

using namespace util;

int main(int argc, char** argv) {
   list::iterator it;
   it.foo();
   return 0;
}
