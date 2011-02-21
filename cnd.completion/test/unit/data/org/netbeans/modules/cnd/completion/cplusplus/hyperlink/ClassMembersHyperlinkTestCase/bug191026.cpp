struct bug191026_B {
    int j;
};

namespace bug191026_N1 {
    template <class T> struct bug191026_B {
        int i;
    };
}

namespace bug191026_N2 {
    
    using namespace bug191026_N1;
    
    struct bug191026_A : public bug191026_B<int> {
    };

    void bug191026_foo(){
        bug191026_A a;
        a.i;
    }
}