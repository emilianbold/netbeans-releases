namespace bug235102_3 {
  struct BBB235102_3 {
    int b;
    #include "bug235102_3_body.h"
  };
  
  int BBB235102_3::foo235102_3() {
    return b;
  }
}