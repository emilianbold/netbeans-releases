class Base {
    /* skip */
};

template <class ParentFunctor, typename Fun>
class FunctorHandler
: public ParentFunctor::Impl {
    typedef typename ParentFunctor::Impl Base; // "Base" is resolved wrongly

public:
    typedef typename Base::ResultType ResultType; // "ResultType" in unresolved
    typedef typename Base::Parm1 Parm1; // "Parm1" in unresolved
    /* skip */
};
