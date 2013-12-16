namespace bug224031 {
  
    typedef auto foo(int x) -> decltype(x + x);

    auto boo(int x) -> decltype (x * x);

    auto coo(int x) -> decltype(x / x) {
        return 0;
    }

    int opCode;

    decltype(opCode) (*get_ptr(const char opCode))(int, int);
 
    typedef void (*funPointer)(const int items[], int arg, int elems[]);
}