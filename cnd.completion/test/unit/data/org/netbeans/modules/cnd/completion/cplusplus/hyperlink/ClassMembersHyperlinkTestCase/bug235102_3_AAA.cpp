namespace bug235102_3 {
  struct AAA235102_3 {
    int a;
    #include "bug235102_3_body.h"
  };
  
  int AAA235102_3::foo235102_3() {
    return a;
  }
}