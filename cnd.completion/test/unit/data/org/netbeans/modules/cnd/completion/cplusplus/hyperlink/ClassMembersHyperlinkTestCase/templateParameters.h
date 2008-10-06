template<unsigned int L, class T, template<class> class C>
    class OrderedStatic<L, T, Loki::NullType> : public Private::OrderedStaticBase<T>
    {
    public:
        OrderedStatic() : Private::OrderedStaticBase<T>(L)
        {
            OrderedStaticManager::Instance().registerObject
                                (L,this,&Private::OrderedStaticCreatorFunc::createObject);
        }

        C createObject()
        {
            Private::OrderedStaticBase<T>::SetLongevity(new C<T>);
        }

    private:
        OrderedStatic(const OrderedStatic&);
        OrderedStatic& operator=(const OrderedStatic&);
    };

    template <template <class, class> class ThreadingModel,
              class MX = LOKI_DEFAULT_MUTEX >
    struct RefCountedMTAdj
    {
        template <class P>
        class RefCountedMT : public ThreadingModel< RefCountedMT<P>, MX >
        {
            typedef ThreadingModel< RefCountedMT<P>, MX > base_type;
            typedef typename base_type::IntType       CountType;
            typedef volatile CountType               *CountPtrType;
        };

        // template forward class declaration
        template
        <
            typename TT,
            template <class> class OwnershipPolicy = RefCounted,
            class ConversionPolicy = DisallowConversion,
            AAAAA::BBB::CCC::DDD CheckingPolicy = AssertCheck,
            template <class> class StoragePolicy = DefaultSPStorage,
            unsigned long ConstnessPolicy = LOKI_DEFAULT_CONSTNESS
         >
         class SmartPtr;

    };

// template forward class declaration
template<unsigned int LL, class TT, class TList = Loki::NullType> class OrderedStatic;

template <
            typename T,
            template <class> class OwnershipPolicy = RefCounted,
            class ConversionPolicy = DisallowConversion,
            AAAAA::BBB::CCC::DDD CheckingPolicy = AssertCheck,
            template <class> class StoragePolicy = DefaultSPStorage,
            unsigned long ConstnessPolicy = LOKI_DEFAULT_CONSTNESS
         >
         class SmartPtr {
    
};

template<unsigned int L, class T, class TList = Loki::NullType> class OrderedStatic {

};


template
<
class T,
template <class> class CreationPolicy,
template <class> class L,
template <class, class> class M,
class X
>
class SingletonHolder {
public:
    void DestroySingleton();
};

template
<
class T2,
template <class> class CreationPolicy2,
template <class> class L2,
template <class, class> class M2,
class X2
>
void
SingletonHolder<T2, CreationPolicy2, L2, M2, X2>::DestroySingleton() {
    T2 t;
}

// IZ 144050 : inner type should have priority over global one
class A {};
template <class A> class B { 
    A::sometype member;
};

// IZ#144881: template parameter is not resolved in nested class
template<typename _Tp, typename _Alloc>
struct _Vector_base {
    struct _Vector_impl
            : public _Alloc {
        _Tp* _M_start;
        _Tp* _M_finish;
        _Tp* _M_end_of_storage;

        _Vector_impl(_Alloc const& __a)
        : _Alloc(__a), _M_start(0), _M_finish(0), _M_end_of_storage(0) {
        }
    };
};

