void IZ145071_2_foo() {
    struct IZ145071_2_A;
}

void IZ145071_2_foo2() {
    typedef struct IZ145071_2_A Z;
}

void IZ145071_2_foo3() {
    struct IZ145071_2_A;
    typedef struct IZ145071_2_A Z;
}