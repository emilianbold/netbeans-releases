class A {
public:
    void aoo();
};

class B {
public:
    void boo();
    A* operator -> ();
};

class C {
public:
    void coo();
    
    B operator[] (int ind);
    
    B* operator -> (void);
};

template <T> class TA {
public:
    void taoo();
    
    T operator[] (int ind);
    
    T* operator -> (void);    
};