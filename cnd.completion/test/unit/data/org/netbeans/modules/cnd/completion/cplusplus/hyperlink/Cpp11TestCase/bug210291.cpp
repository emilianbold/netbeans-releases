template<class T> class bug210291_Y { /* ... */ };

template<typename... Outer>
struct bug210291_X {
  template<typename... Inner>
  struct bug210291_Y
  {
    typedef int type;
  };
};

void foo() {
    bug210291_X<int, double>::bug210291_Y<short, char>::type x;
}