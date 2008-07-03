template <class T> class C {
public:
    typedef T* pointer;
    typedef pointer p;
    T::nestedtype::nestedtype2 foo(T t) {
        t.sayHi();
        p tt = new T();
        tt->sayHi();
        T::sayHiStatic();
    }
//    void bar() {
//        foo().doSomething();
//    }
};

