template<typename Signature>
struct bug210194_function_traits;

template<typename R, typename... ArgTypes>
struct bug210194_function_traits<R(ArgTypes...)> {
  typedef R result_type;
};

template<typename T, typename U>
struct bug210194_same_type {
  static const bool value = false;
};

template<typename T>
struct bug210194_same_type<T, T> {
  static const bool value = true;
};

int bug210194_a0[bug210194_same_type<bug210194_function_traits<int()>::result_type, int>::value? 1 : -1];
int bug210194_a1[bug210194_same_type<bug210194_function_traits<int(float)>::result_type, int>::value? 1 : -1];
int bug210194_a2[bug210194_same_type<bug210194_function_traits<int(double, char)>::result_type, int>::value? 1 : -1];
