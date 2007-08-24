
#include "file.h"

int main(int argc, char** argv) {
     // here test text is inserted
    S1::foo();
    S1::var1 = 14;
    S1::S2::boo();
    S1::S2::var2 = 105;
    return 0;
}

void usingNS1() {    
    using namespace S1;
    var1 = 10;
    foo();
    clsS1 c1;
    c1.clsS1pubFun();
}

void usingNS1S2() {
    using namespace S1::S2;
    var2 = 10;
    boo();
    clsS2 c2;
    c2.clsS2pubFun();
}

void usingDirectivesS1() {
    using S1::clsS1;
    clsS1 c1;
    using S1::var1;
    var1 = 10;
    using S1::foo;
    foo();
}

void usingDirectivesS1S2() {
    using S1::S2::clsS2;
    clsS2 c2;
    using S1::S2::var2;
    var2 = 10;
    using S1::S2::boo;
    boo();
}

void usingNS2() {
    using namespace S1;
    using namespace S2;
    
    var2 = 10;
    boo();
    clsS2 c2;
    c2.clsS2pubFun();    
}

void usingDirectivesS2() {
    using namespace S1;
    myCout = 10;
    using S2::clsS2;
    clsS2 c2;
    using S2::var2;
    var2 = 10;
    using S2::boo;
    boo();
}

void usingCout() {
    S1::myCout;
    using S1::myCout;
    myCout;
}
