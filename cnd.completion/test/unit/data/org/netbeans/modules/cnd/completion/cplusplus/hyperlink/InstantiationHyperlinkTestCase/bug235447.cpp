namespace bug235447 {
    class foo_235447 {
    public:
        template<typename T2> foo_235447(const T2& f) {
            std::cout << f << std::endl;
        }

        void bar1(const foo_235447& v) {
            foo_235447(v).bar2();
        }

        void bar2() {
        }
    };
}