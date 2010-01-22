class IZ179373_B{
public:
    void f(){

    }
};

class IZ179373_A{
public:
    IZ179373_B operator*(const IZ179373_A& arg) const{
        return IZ179373_B();
    }
};

int IZ179373_main() {
    IZ179373_A b;
    (b * b).f();  //unable to resolve identifier f
    return 0;
}