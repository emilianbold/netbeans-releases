namespace bug235076 {
  
    using A1235076 = int (*)(int a, int b);
  
    template <class T> 
    using Alias_235076 = auto (*)(int) -> T;

    template <class T>
    auto foo_235076(int a) -> T {
        return T();
    }        
}