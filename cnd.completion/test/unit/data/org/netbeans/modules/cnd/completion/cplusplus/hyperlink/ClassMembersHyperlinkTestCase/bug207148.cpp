struct {
   int foo;
   int bar;
} typedef bug207148_MyStruct;

int bug207148_main(int argc, char** argv) {
    bug207148_MyStruct a;
    a.foo++;
    return 0;
}