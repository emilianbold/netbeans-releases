namespace bug240123 {
    template <typename T1>
    struct XXX240123 {    
        template <typename T2>
        struct BBB240123 {
            int foo240123();
        };
    };


    template <> template <>
    int XXX240123<int>::BBB240123<double>::foo240123() {
        int x;
        return x;
    }  
}