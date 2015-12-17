#include <stdio.h>

int foo(int& a) { // C++03
    printf("+ %d\n", a);
    return 40;
}

int foo(int&& a) { // C++11
    printf("- %d\n", a);
    return 40;
}

int main() {
    int x = 1;
    foo(1.0 + 2.0);
    foo(x);
    return 0;
}