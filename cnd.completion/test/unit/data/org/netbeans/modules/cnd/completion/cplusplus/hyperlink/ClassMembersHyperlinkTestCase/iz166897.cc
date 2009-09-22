typedef struct iz166897_Type12
{
    Type12() : _val(1){}
    void foo1();
    int _val;
} iz166897_DefinedType;


void iz166897_DefinedType::foo1()
{
    // Unable to resolve variable _val
    std::cout << _val << std::endl;
}
