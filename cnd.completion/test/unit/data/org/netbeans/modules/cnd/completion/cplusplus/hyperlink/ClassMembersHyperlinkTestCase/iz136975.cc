struct MyType {
    int Release(MyType * pointer);
    void Destroy();
    void OnDereference();

    MyType * operator->() {
        return this;
    }
};

class SmartPtr {
    typedef MyType SP;
    typedef MyType OP;
    typedef MyType KP;
    typedef MyType* PointerType;

    ~SmartPtr() {
        if (OP::Release(*static_cast<SP*> (this))) {
            SP::Destroy();
        }
    }

    PointerType operator->() {
        KP::OnDereference();
        return SP::operator->();
    }
};
