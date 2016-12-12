namespace bug269272 {
    enum SomeEnum {
        ns_empty, ns_decltype
    };

    template <SomeEnum ns>
    class SomeClass;

    class Base {
    protected:
        void myfun();
    };

    template <>
    class SomeClass<ns_empty> : public Base {
        typedef SomeClass<ns_decltype> type;

        void foo(void *p) {
            static_cast<SomeClass*>(p)->myfun();
        }
    };


    template <>
    class SomeClass<ns_decltype> : public Base {
        typedef SomeClass<ns_empty> type;

        void foo(void *p) {
            static_cast<SomeClass*>(p)->myfun();
        }
    }; 
}