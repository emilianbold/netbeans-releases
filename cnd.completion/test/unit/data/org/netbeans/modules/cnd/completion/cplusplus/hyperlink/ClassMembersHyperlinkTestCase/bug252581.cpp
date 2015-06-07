namespace bug252581 {
    template <typename T>
    class Base252581
    {
    public:
        void foo() {}
        void bar() {}
    };

    template <typename T>
    class Child252581 : private Base252581<T>
    {
        typedef Base252581<T> base;

    public:
        using base::foo;
    };

    void roo252581() {
        Child252581<int> child;
        child.foo(); 
    }
}