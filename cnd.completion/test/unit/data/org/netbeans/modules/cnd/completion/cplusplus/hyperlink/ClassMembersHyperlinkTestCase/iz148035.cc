template <class T> class iz148035_Container {
public:
    T* operator->() { return 0; }
};

template <class T> class iz148035_A {
public:
    typedef iz148035_Container<T> elem_t;
};

class iz148035_B {
public:
    void bb();
};

class iz148035_C : public iz148035_A<iz148035_B> {
public:
    void cc() {
        elem_t elem;
        elem->bb(); // "bb" is not resolved
    }
};
