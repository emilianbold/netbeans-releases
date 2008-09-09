

struct StrWithMembers {
    void foo();
    int aaa;
};

void methodToAcceptMethods(void (*fun)()) {
    void (*f)() = &StrWithMembers::foo;
    methodToAcceptMethods(&StrWithMembers::foo);
}

void intMethod(int* val) {
    int *v = &StrWithMembers::aaa;
    intMethod(&StrWithMembers::aaa);
}
