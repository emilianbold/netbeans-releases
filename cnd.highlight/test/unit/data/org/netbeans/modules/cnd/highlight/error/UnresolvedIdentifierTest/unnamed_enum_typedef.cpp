class D
{
public:
    typedef enum
    {
        MyEnum_ValOne, // <--- Unable to resolve identifier
        MyEnum_ValTwo  // <--- Unable to resolve identifier
    } MyEnum;
};

int main() {
    D::MyEnum k = D::MyEnum_ValOne;
    return 0;
}
