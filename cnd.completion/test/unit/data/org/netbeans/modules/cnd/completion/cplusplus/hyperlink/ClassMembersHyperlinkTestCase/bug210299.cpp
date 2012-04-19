template<typename Args>
struct bug210299_B {
  static const int value = 0;
};

template<typename Args>
struct bug210299_A : bug210299_B<Args*> { };

int bug210299_i = bug210299_A<int>::value;