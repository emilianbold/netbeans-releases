namespace bug262407 {
    template<int _Len, int _Align>
    struct aligned_storage262407 {
        typedef int type;
    };

    typename aligned_storage262407<10, alignof(void*)>::type x262407; //unable to resolve symbol `type`
}