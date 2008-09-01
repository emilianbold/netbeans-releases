
    class Bass {
    public:
        virtual ~Bass() {}
        virtual void method1() {  }
        int field1;
    protected:
        virtual void method11() {  }
        int field11;
    };

    class Derived : public Bass {
    public:
        Derived() {}
        virtual ~Derived() {}
        virtual void method1() {  }
        void method2();
    };

    void Derived::method2()
    {
        this->Bass::method11();
        (*this).Bass::field11;
        Bass::method1();
        Bass::method11();
        Bass* bbb;
        bbb->method11(); // not visible in this context!
    }

    int check() {
        Derived* dd;
        dd->Bass::method1();
        dd->method1();
        dd->Bass::field1;
        dd->Bass::field11; // not visible!
        dd->field1;
        dd->field11; // not visible!
        (*dd).Bass::method1();
        (*dd).Bass::method11(); // not visible!
    }
