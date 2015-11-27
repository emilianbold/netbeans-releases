namespace bug256700 {
    namespace some_ns256700 {
        template <class _Tp>
        struct _Nonconst_traits256700;

        template <class _Tp>
        struct _Nonconst_traits256700 {
          typedef _Tp value_type;
          typedef _Tp* pointer;
          typedef _Nonconst_traits256700<_Tp> _NonConstTraits;
        };
    }

    #define DEFINE_PRIV_TRAITS \
    namespace priv256700 { \
       template <class _Tp> struct _MapTraitsT256700 ; \
       template <class _Tp> struct _MapTraitsT256700 : public :: bug256700 :: some_ns256700 :: _Nonconst_traits256700 <_Tp> { \
           typedef _MapTraitsT256700 <_Tp> _NonConstTraits; \
       }; \
    } 

    DEFINE_PRIV_TRAITS

    struct AAA256700 {
        void foo();
    };

    template <typename Traits> 
    struct MapBase256700 {
        typedef typename Traits::_NonConstTraits NonConstTraits;
    };

    template <typename Value>
    struct Map256700 {
        typedef Value value_type;
        typedef typename priv256700::_MapTraitsT256700<value_type> _MapTraits;
        typedef MapBase256700<_MapTraits> RepType;
    };

    int main256700() {
        Map256700<AAA256700>::RepType::NonConstTraits::value_type var;
        var.foo();
        Map256700<AAA256700>::RepType::NonConstTraits::pointer ptr;
        ptr->foo();
        return 0;
    }
}