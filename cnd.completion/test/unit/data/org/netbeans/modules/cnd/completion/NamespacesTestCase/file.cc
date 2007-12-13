
#include "file.h"

int main(int argc, char** argv) {
     // here test text is inserted
    return 0;
}

namespace S1 {
    
    void f1() {
        f1();
        S2::i2 = 11;
        S2::f2();
        i1 = 0;
    }
    
    namespace S2 {
        void f2() {
            f1();
            f2();
            i2 = 10;
            i1 = 1;
            S1::i1 = 2;
            S2::i2 = 3;
            S1::f1();
            S2::f2();
        }

        struct s2Struct {
            int f;
        } sss;
    }
}


void usingS1() {
    using namespace S1;
     //
}

void usingS2() {
     //
     //
}

void usingS1S2() {
    using namespace S1;
    using namespace S2;
     //
}

namespace AliasS1 = S1;
namespace AliasS2 = S1::S2;

void aliases() {
     //
}

void innerNS3Func() {
     //
}