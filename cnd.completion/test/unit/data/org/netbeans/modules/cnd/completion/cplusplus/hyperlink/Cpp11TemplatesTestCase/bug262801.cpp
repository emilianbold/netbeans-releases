namespace bug262801 {
    template <bool val>
    struct AAA262801 {
        void roo();
    };

    template <typename T>
    struct container262801 {
        container262801(T param1, T param2);
        int size();
    };

    void fun262801() {
        auto var1 = container262801<container262801<const char*>>{{"one", "two"}, {"three", "four"}};
        var1.size();
        AAA262801< (1 > 5 && 1 > 6) >().roo(); // roo is unresolved
    }
}
