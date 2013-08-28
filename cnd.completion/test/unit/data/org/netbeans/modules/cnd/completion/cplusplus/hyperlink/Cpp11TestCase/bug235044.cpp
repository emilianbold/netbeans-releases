namespace bug235044 {
    
    struct AAA_bug235044 {
        int foo();
    };

    template <class T> using A1_bug235044 = T;

    struct BBB_bug235044 {
        template <class T> using A2_bug235044 = T;
    };

    template<class T>
    struct CCC_bug235044 {
        using A3_bug235044 = T;
    };

    template <class _T> using A4_bug235044 = typename CCC_bug235044<_T>::A3_bug235044;

    template<class T1, class T2>
    struct DDD_bug235044 {
        typedef T1 type1;
        typedef T2 type2;
    };

    template <class T>
    struct EEE_bug235044 {
        template <class _T> using A5_bug235044 = typename DDD_bug235044<T, _T>::type1;
        template <class _T> using A6_bug235044 = typename DDD_bug235044<T, _T>::type2;
    };


    int foo_bug235044() { 
        A1_bug235044<AAA_bug235044> a; 
        a.foo();

        BBB_bug235044::A2_bug235044<AAA_bug235044> b;
        b.foo(); 

        CCC_bug235044<AAA_bug235044>::A3_bug235044 c;
        c.foo(); 

        A4_bug235044<AAA_bug235044> d;
        d.foo();

        EEE_bug235044<AAA_bug235044>::A5_bug235044<int> e;
        e.foo();

//        EEE_bug235044<int>::A6_bug235044<AAA_bug235044> f;
//        f.foo();
    }  
}
