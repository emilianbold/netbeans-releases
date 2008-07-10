template <class TT> class DefaultSPStorage
{
public:
    template <class UT> DefaultSPStorage(const DefaultSPStorage<UT>&) {}
    template<T2> void useT2Declaration(T2 o);
};

template<typename UU> void useT2Declaration(UU o);
template<typename UU> void useT2Declaration(UU o) {}

template<T2> void DefaultSPStorage::useT2Declaration(T2 o) {}

template<class From, class To> To convert(From);
