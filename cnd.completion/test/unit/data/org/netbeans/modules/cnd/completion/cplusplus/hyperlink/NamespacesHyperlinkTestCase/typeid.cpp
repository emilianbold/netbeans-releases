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

    int bug219398_main(int argc, char** argv) {
        struct Node
        {
        } node1, node2;
        // "Unable to resolve identifier name" mark appear and source code format
        if (typeid (node1).name() == typeid (struct Node) .name()) {
        }

        return 0;
    }

}