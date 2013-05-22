
namespace {

    struct bug_230079_MyClass {
      void bug_230079_Test() {}
    }

    template <class T>
    bug_230079_MyClass* bug_230079_foo() {

    }

    int bug_230079_boo() {
      (*bug_230079_foo<int>()).bug_230079_Test(); // unresolved identifier Test
    }
    
}