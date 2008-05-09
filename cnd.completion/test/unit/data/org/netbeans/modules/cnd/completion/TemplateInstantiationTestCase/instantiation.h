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

