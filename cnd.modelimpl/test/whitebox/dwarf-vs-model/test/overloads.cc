#include "overloads.h"

void foo(unsigned int u) {
}


void foo(int p) {
}

void foo() {
}

void foo(char c) {
}


int main() {
    struct foo {
	int x;
    };

    foo f;
    return f.x;
}
