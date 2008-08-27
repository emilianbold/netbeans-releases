template
<
    class T,
    template <class> class C,
    template <class> class L,
    template <class, class> class M,
    class X
>
class SingletonHolder {
public:
    typedef void* PtrInstanceType;
    static PtrInstanceType pInstance_;
};

template
<
    class T,
    template <class> class C,
    template <class> class L,
    template <class, class> class M,
    class X
>
typename SingletonHolder<T, C, L, M, X>::PtrInstanceType
    SingletonHolder<T, C, L, M, X>::pInstance_;
