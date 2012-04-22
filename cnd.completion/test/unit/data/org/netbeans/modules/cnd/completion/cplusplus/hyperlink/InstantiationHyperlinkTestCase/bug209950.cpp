template<typename T>
struct bug209950_X0 {
  template<typename U>
  struct bug209950_InnerTemplate : public T { };
};

template<>
template<>
struct bug209950_X0<void*>::bug209950_InnerTemplate<int> { };
