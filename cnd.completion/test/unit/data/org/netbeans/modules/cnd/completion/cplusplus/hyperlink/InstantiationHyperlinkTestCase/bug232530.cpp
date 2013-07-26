namespace {
    template <int size> 
    struct AAA {
        template <typename _G>
        int deref(const _G &value);     
    };

    template <> template <typename _G>
    int AAA<8>::deref<_G>(const _G &value) {
        return 0;
    }
}
