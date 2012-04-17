template<typename Signature>
struct bug210257_function_traits;

template<typename R, typename... ArgTypes>
struct bug210257_function_traits<R(ArgTypes......)> {
  typedef R result_type;
};

bug210257_function_traits<int(double, char...)>::result_type t;
