namespace ns207841_1 {
    struct BBB207841;
    namespace ns207841_2 {
        struct CCCBBB207841 {
            const BBB207841* zoo();
        };
    }
}

namespace ns207841_1 {
    struct BBB207841 {
        void boo() const;
        void boo2() const {}
    };
    namespace ns207841_2 {
        struct SS207841 {
            void foo();
            void foo2() {}
        };
    } 
}

using namespace ns207841_1;
using namespace ns207841_2;

void BBB207841::boo() const {
    boo2();
}

void SS207841::foo() {
    foo2();
}

const BBB207841* CCCBBB207841::zoo() {
    const BBB207841* ptr;
    ptr->boo();
    return ptr;
}

int checkFunction207841() {
    SS207841 aa;
    aa.foo();
    BBB207841 bb;
    bb.boo();
}
