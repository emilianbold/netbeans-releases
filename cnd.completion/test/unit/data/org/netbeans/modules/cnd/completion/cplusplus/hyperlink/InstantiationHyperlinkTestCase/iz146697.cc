
template<typename _Tp>
class allocator {
public:
    typedef _Tp& reference;

    template<typename _Tp1>
    struct rebind {
        typedef allocator<_Tp1> other;
    };
};

template<typename _Tp, typename _Alloc>
struct _Vector_base {
    typedef typename _Alloc::template rebind<_Tp>::other _Tp_alloc_type; // !OK
    //typedef typename _Alloc _Tp_alloc_type; // OK
};

template<typename _Tp, typename _Alloc = allocator<_Tp> >
        class vector : protected _Vector_base<_Tp, _Alloc> {
public:
    typedef _Vector_base<_Tp, _Alloc> _Base;
    typedef typename _Base::_Tp_alloc_type _Tp_alloc_type;
    typedef typename _Tp_alloc_type::reference reference;

    reference
    operator[](int __n) {
        return 0;
    }
};

class string {
public:
    char c;
};

typedef vector<string> StringList;

int foo() {
    StringList sl;
    char s = sl[0].c;
}
