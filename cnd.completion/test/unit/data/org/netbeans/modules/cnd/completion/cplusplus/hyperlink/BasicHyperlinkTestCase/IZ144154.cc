namespace detail {

    template<typename T>
    struct member_char_type {
        typedef typename T::char_type type;
    };

    template<typename T>
    class unwrap_reference
    {
     public:
        typedef T type;
    };

    template<typename T>
    struct unwrapped_type
    : unwrap_reference<T>
    { };
}

template<typename T>
struct char_type_of
    : detail::member_char_type<
          typename detail::unwrapped_type<T>::type // type unresolved
      >
    { };

template<typename T>
struct next
{
    typedef typename T::next type;
};

template<
      typename Size
    , typename T
    , typename Next
    >
struct l_item
{
    typedef l_item type;
    typedef Size size;
    typedef T item;
    typedef Next next;
};

namespace boost { namespace mpl {

template<class T> struct push_front_impl {};

template<>
struct push_front_impl<int>
{
    template< typename List, typename T > struct apply
    {
        typedef l_item<
              typename next<typename List::size>::type // type unresolved
            , T
            , typename List::type
            > type;
    };
};

}}
