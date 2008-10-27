class A
{
    A();
    ~A();
private:
    class B;
};

class A::B
{
public:
    B();
    ~B();
};

A::B::~B() {}
