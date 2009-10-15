struct iz159422_A {
public:
    typedef iz159422_A& F(int t); // unresolved
    F foo;
    int i;
};

iz159422_A& iz159422_A::foo(int t) // unresolved
{
    i++; // unresolved
    return *this;
}

int iz159422_main() {
    iz159422_A a;
    a.foo(1).foo(1).foo(1).i++;
    return 0;
}