// #include <stdio.h>
// #include <stdlib.h>


//
// Variables
//

static int A;
int* pA(&A);
int** ppA(&pA);
int*** pppA(&ppA);

int f2(*pA);
int f3(**ppA);
int f4(***pppA);

int a(A);
int* pa(&A);
int foo_pa(*pa);

int foo_1(A);

int foo_2(optind);

namespace qwe {
    int q;
    namespace asd {
        int a;
        namespace zxc {
            int z;
            int qweqwe(q);
            int asdasd(a);
            int zxczxc(a);
        }
    }
}

using namespace qwe;
using namespace qwe::asd;
using namespace qwe::asd::zxc;


//
// Functions
//

class C {};

int f11(C);
int f23(C*);

int main(int argc, char** argv) {
    int foo;
    foo = qweqwe;
    foo = asdasd;
    foo = zxczxc;
    return foo;
}
