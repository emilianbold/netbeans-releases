
struct VectorElement {
    void foo();
};

template<typename _Tp> class allocator {
public:
    typedef _Tp& reference;
};

template<typename _Tp, typename _Alloc = allocator<_Tp> >
        class vector {
public:
    typedef typename _Alloc::reference reference;
    reference operator[] (int index);
};

void use_vector() {
    vector<VectorElement> v;
    v[2].foo();
}
