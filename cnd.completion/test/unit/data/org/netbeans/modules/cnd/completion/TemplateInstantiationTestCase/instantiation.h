template<typename _Key, typename _Val> class pair {
public:
       _Key getKey();
       _Val getValue();
};

template<typename _Key, typename _Val> class _Rb_tree {
public:
       _Key key_OK();
       _Val val_OK();
};

template <typename _Key, typename _Tp> class MYmap {
public:
    typedef _Key                                          key_type;
    typedef pair<const _Key, _Tp>                         value_type;
    typedef _Rb_tree<key_type, value_type> _Rep_type;
    typedef typename _Rep_type::iterator               iterator;
    _Key key_BAD();
    _Tp tp_BAD();
    key_type td_key_BAD();
    value_type td_pair_BAD();
};

class A {
public:
    void foo();
};

class B {
public:
    void boo();
};

template<typename _Iterator> class My__normal_iterator {
protected:
    _Iterator _M_current;

public:
    typedef typename _Iterator::pointer pointer;

    pointer
    operator->() const {
        return _M_current;
    }
};

template<typename _Tp> class MyAllocator {
public:
    typedef _Tp* pointer;
};

template<typename _Tp, typename _Alloc = MyAllocator<_Tp> > class MyVector {
public:
    typedef _Tp value_type;
    typedef typename _Alloc::pointer pointer;
    typedef My__normal_iterator<pointer> iterator;

    void push_back(const value_type& __x);
};
