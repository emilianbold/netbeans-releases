struct IZ158816_CName {
    int i;
};

#define IZ158816_C IZ158816_CName

void IZ158816_foo() {
    IZ158816_C.i; // unresolved
}