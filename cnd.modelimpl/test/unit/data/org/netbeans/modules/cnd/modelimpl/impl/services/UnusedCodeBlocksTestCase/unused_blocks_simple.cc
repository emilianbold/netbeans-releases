#undef MODE

#ifdef MODE
void foo_1();
#else
void foo_2();
#endif

#define MODE 1

#ifdef MODE
void foo_3();
#else
void foo_4();
#endif
