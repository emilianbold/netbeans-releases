#include <stdio.h>

class A {
public:
    A() {}
};

int main(int argc, char** argv) {
    A a;
    printf("Value %p\n", &a);
    return 0;
}