#include "typeinfo.h"
namespace iz162160 {
    using namespace std;

    struct A {

        virtual ~A() {
        }
    };

    struct B : A {
    };

    struct C {
    };

    struct D : C {
    };

    int main() {
        B bobj;
        A* ap = &bobj;
        A& ar = bobj;
        typeid (*ap).name();
        typeid (typeid(ar).name()).name();

        D dobj;
        C* cp = &dobj;
        C& cr = dobj;
        typeid (*cp).name();
        typeid (cr).name();
    }
}