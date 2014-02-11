namespace bug241651 {
    template <class T> using TemplateAlias241651 = T;

    struct Class1_241651 {
        virtual int foo();
    };

    struct Class2_241651 : Class1_241651 {
        virtual int foo();
    };

    typedef Class1_241651 XXX241651;

    int main241651() {
        int XXX241651 = 1;   
        int a = Class2_241651().foo();
        int b = Class2_241651().TemplateAlias241651<Class1_241651>::foo();
        int c = Class2_241651().XXX241651::foo();
        int d = Class2_241651().Class1_241651::foo();
        return a + b + c + d - XXX241651;
    }
}