namespace {
template <typename _A> 
struct AAA {
    
    template <typename _B>
    struct BBB {
        
        template <typename _C>
        struct CCC {
            
            struct DDD {
                DDD(int a);
            };
        };
        
        template <typename _G>
        int deref(const _G &value);  
    };    
};

template <typename _A> template <typename _B> template <typename _X>
AAA<_A>::BBB<_B>::CCC<_X>::DDD::DDD(int a) {
    
}

template <typename _A> template <typename _B> template <typename _G>
int AAA<_A>::BBB<_B>::deref(const _G &value) {
    
}
}