#include "namespaces.h"

namespace ns1 {
    int foo() {
    	int x = 7;
	int y = 8;
	return x + y;
    }
    int foo_2();
}

int ns1::foo_2() {
    int z = foo();
    return z;
}

namespace NS {
	void foo_1() {
	}
}

void NS::foo_2() {
}