#include "bug239348.h"

namespace bug239348 {

  struct BB {
    int xx;
  };

  int foo() {
      AA a;
      a.pb->xx;
  }

}