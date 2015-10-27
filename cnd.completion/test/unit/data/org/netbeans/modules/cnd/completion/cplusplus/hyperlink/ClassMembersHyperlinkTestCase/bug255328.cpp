struct MyStruct255328 {
    int field;
};

typedef struct { 
   int  value;
} S255328;

typedef struct {
   void *c;
   S255328 s[1]; 
} T255328;

T255328 s255328[]={
   [0] = {(void*)(0 ? 1 : 2, 3), {{.value = 0}}},
   [1] = {(void*)1, {{value : 0}}},
   {&(MyStruct255328) {.field = 0}, {{.value = 0}}}
};