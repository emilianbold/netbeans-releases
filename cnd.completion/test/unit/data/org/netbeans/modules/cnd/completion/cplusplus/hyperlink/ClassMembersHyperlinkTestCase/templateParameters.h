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
