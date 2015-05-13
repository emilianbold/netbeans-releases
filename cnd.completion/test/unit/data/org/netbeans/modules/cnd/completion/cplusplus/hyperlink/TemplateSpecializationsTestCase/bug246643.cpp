namespace bug246643 {
    namespace std246643 {
        template <typename T1, typename T2> 
        struct pair246643 {
            T1 first;
            T2 second;
        };
    }
    template <typename T, unsigned N> 
    class SmallVector246643 {
        T& operator[](int index);
    };

    void foo246643() const {

        class CallBack246643 {
        public:
            SmallVector246643<std246643::pair246643<int, int>, 10> V;
        };
        CallBack246643 cb;
        cb.V[1].second;
    }
}