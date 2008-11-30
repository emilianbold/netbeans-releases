
// IZ#151955: java.lang.StackOverflowError in boost 1.36

class C {
};

template< typename T >
struct msvc_eti_base : T
{
    typedef T type;
};

struct size : msvc_eti_base<C>::type
{
};
