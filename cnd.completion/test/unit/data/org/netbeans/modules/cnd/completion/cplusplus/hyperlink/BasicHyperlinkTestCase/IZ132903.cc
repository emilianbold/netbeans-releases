template <class T> class comPtr : public T {
protected:
    comPtr();
private:
};

class AAA {
public:
    void foo();
    void boo();
    void zoo();
};

void method() {
    comPtr<AAA> aaa;
    aaa.foo();
}
