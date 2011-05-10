template <class T>
struct bug158905_B;

class bug158905_A {
    int i;
    friend struct bug158905_B<int>;
};

template <>
struct bug158905_B<int> {
    void foo() {
        bug158905_A a;
        a.i++; // unresolved
    }
};