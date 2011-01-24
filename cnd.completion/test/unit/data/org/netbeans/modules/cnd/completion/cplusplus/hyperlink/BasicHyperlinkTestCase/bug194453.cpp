
struct {
  int a;
  int b;
  struct {
     int c;
     int d;
  } bug194453_mySubStruct;
} bug194453_myStruct = { .b = 5, .bug194453_mySubStruct = { .d = 5 } };
