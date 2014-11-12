namespace bug248600 {
    template<typename _Tp, typename _Up>
    struct __ptrtr_rebind248600
    {
      // Not used in that test case
      typedef void __type;  
    };

    /**
     * @brief  Uniform interface to all pointer-like types
     * @ingroup pointer_abstractions
    */
    template<typename _Ptr>
    struct pointer_traits248600
    {
      /// The pointer type
      typedef _Ptr pointer;

      template<typename _Up>
      using rebind = typename __ptrtr_rebind248600<_Ptr, _Up>::__type;
    };

    template<typename _Alloc, typename _Tp>
    class __alloctr_rebind_helper248600
    {
      template<typename _Alloc2, typename _Tp2>
            static constexpr bool
            _S_chk(typename _Alloc2::template rebind<_Tp2>::other*)
            { return true; }

      template<typename, typename>
        static constexpr bool
            _S_chk(...)
            { return false; }

    public:
      static const bool __value = _S_chk<_Alloc, _Tp>(nullptr);
    };

    template<typename _Alloc, typename _Tp, bool = __alloctr_rebind_helper248600<_Alloc, _Tp>::__value>
    struct __alloctr_rebind248600;

    template<typename _Alloc, typename _Tp>
    struct __alloctr_rebind248600<_Alloc, _Tp, true>
    {
      typedef typename _Alloc::template rebind<_Tp>::other __type;
    };

    template<template<typename, typename...> class _Alloc, typename _Tp, typename _Up, typename... _Args>
    struct __alloctr_rebind248600<_Alloc<_Up, _Args...>, _Tp, false>
    {
      typedef _Alloc<_Tp, _Args...> __type;
    };

    template<typename _Alloc>
    struct allocator_traits248600
    {
      /// The allocator type
      typedef _Alloc allocator_type;
      /// The allocated type
      typedef typename _Alloc::value_type value_type;

    private: 
      template<typename _Tp> 
      static typename _Tp:: pointer _S_pointer_helper (_Tp*);
      static value_type* _S_pointer_helper (...);
      typedef decltype( _S_pointer_helper ((_Alloc*)0)) __pointer ;
    public:

      /**
       * @brief   The allocator's pointer type.
       *
       * @c Alloc::pointer if that type exists, otherwise @c value_type*
      */
      typedef __pointer pointer;

    private: 
      template<typename _Tp> 
      static typename _Tp:: const_pointer _S_const_pointer_helper (_Tp*);
      static typename pointer_traits248600<pointer>::template rebind<const value_type> _S_const_pointer_helper (...);
      typedef decltype( _S_const_pointer_helper ((_Alloc*)0)) __const_pointer ;
    public:

      /**
       * @brief   The allocator's const pointer type.
       *
       * @c Alloc::const_pointer if that type exists, otherwise
       * <tt> pointer_traits<pointer>::rebind<const value_type> </tt>
      */
      typedef __const_pointer const_pointer;

      template<typename _Tp>
      using rebind_alloc = typename __alloctr_rebind248600<_Alloc, _Tp>::__type;  
    };

    template<typename _Alloc>
    struct __alloc_traits248600 {

        typedef allocator_traits248600<_Alloc>           _Base_type;

        template<typename _Tp>
        struct rebind { 
            typedef typename _Base_type::template rebind_alloc<_Tp> other; 
        };    
    };

    template <typename T, typename Alloc>
    struct Proxy248600 {
        typedef typename __alloc_traits248600<Alloc>::template
            rebind<T>::other _Tp_alloc_type;
    };

    template<typename _Tp>
    class allocator248600 {
    public:
        typedef int size_type;
        typedef _Tp* pointer;
        typedef const _Tp* const_pointer;
        typedef _Tp& reference;
        typedef const _Tp& const_reference;
        typedef _Tp value_type;

        template<typename _Tp1>
        struct rebind {
            typedef allocator248600<_Tp1> other;
        };
    };

    struct AAA248600 {
        int foo() const;
    };

    typedef typename Proxy248600<AAA248600, allocator248600<AAA248600>>::_Tp_alloc_type _Tp_alloc_type248600;

    typedef allocator_traits248600<_Tp_alloc_type248600> AllocTraits248600;

    int main248600() {
        AllocTraits248600::const_pointer ptr2;
        ptr2->foo();
    }   
} 