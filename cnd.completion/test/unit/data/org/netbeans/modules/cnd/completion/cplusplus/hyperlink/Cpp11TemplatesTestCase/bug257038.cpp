namespace bug257038 {
    namespace std257038 {
      template <typename T>
      struct unique_ptr257038 {
          T* operator->();
      };
        
      template<typename _Tp>
        struct _MakeUniq
        { typedef unique_ptr257038<_Tp> __single_object; };

      template<typename _Tp>
        struct _MakeUniq<_Tp[]>
        { typedef unique_ptr257038<_Tp[]> __array; };

      template<typename _Tp, int _Bound>
        struct _MakeUniq<_Tp[_Bound]>
        { struct __invalid_type { }; };

      /// std::make_unique for single objects
      template<typename _Tp, typename... _Args>
        inline typename _MakeUniq<_Tp>::__single_object
        make_unique257038(_Args&&... __args);

      /// std::make_unique for arrays of unknown bound
      template<typename _Tp>
        inline typename _MakeUniq<_Tp>::__array
        make_unique257038(int __num);

      /// Disable std::make_unique for arrays of known bound
      template<typename _Tp, typename... _Args>
        inline typename _MakeUniq<_Tp>::__invalid_type
        make_unique257038(_Args&&...) = delete;
    }
    
    using namespace std257038;

    struct S257038 {
        S257038() {} 
        void foo() {} 
    };

    int main257038() {
        auto p2 = make_unique257038<S257038>();
        p2->foo(); // unresolved identifier 'foo'
    }  
}
