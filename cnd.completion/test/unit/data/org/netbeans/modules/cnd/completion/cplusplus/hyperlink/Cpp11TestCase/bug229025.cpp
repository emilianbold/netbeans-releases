namespace bug229025 {
    struct A {};
    template<class T> struct foo { const static bool bar = false; typedef T type;};
    template<class T> struct foo<T&> { const static bool bar1 = true; };
    template<class T> struct foo<T&&> { const static bool bar2 = true; };

    void main() {
        foo<A>::bar;
        foo<A&>::bar1;
        foo<A&&>::bar2;
    }
}
