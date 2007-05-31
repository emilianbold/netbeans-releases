
struct __true_type {};
struct __false_type {};

// The following could be written in terms of numeric_limits.
// We're doing it separately to reduce the number of dependencies.

template <class _Tp>
  struct _Is_integer
  {
    typedef __false_type _Integral;
  };

template<>
  struct _Is_integer<bool>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<char>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<signed char>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<unsigned char>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<wchar_t>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<short>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<unsigned short>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<int>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<unsigned int>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<long>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<unsigned long>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<long long>
  {
    typedef __true_type _Integral;
  };

template<>
  struct _Is_integer<unsigned long long>
  {
    typedef __true_type _Integral;
  };

template<typename _Tp>
  struct _Is_normal_iterator
  {
    typedef __false_type _Normal;
  };
  
