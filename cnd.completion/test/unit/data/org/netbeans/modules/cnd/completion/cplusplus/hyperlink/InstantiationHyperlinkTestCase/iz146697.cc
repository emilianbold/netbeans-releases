
template<typename _Tp>
class iz_146697_allocator {
public:
    typedef _Tp& reference;

    template<typename _Tp1>
    struct rebind {
        typedef iz_146697_allocator<_Tp1> other;
    };
};

template<typename _Tp, typename _Alloc>
struct iz_146697__Vector_base {
    typedef typename _Alloc::template rebind<_Tp>::other _Tp_alloc_type; // !OK
    //typedef typename _Alloc _Tp_alloc_type; // OK
};

template<typename _Tp, typename _Alloc = iz_146697_allocator<_Tp> >
        class iz_146697_vector : protected iz_146697__Vector_base<_Tp, _Alloc> {
public:
    typedef iz_146697__Vector_base<_Tp, _Alloc> _Base;
    typedef typename _Base::_Tp_alloc_type _Tp_alloc_type;
    typedef typename _Tp_alloc_type::reference reference;

    reference
    operator[](int __n) {
        return 0;
    }
};

class iz_146697_string {
public:
    char c;
};

typedef iz_146697_vector<iz_146697_string> iz_146697_StringList;

int iz_146697_foo() {
    iz_146697_StringList sl;
    char s = sl[0].c;
}
