
#include "file.h"

void ClassC::cPubFun() {
    ClassA a;
    ClassB b; 
     //  <- test text is inserted here
}

void friendOfB() {
    ClassA a;
    ClassB b; 
    ClassD d;
     //  <- test text is inserted here
}

void ClassC::cProtFun() {
    ClassE e; 
     //  <- test text is inserted here
}

void ClassA::aPubFun() {
    ClassB b;
     //  <- test text is inserted here
}

void ClassD::dPubFun() {
    ::ClassA a; 
    ClassB b;
    ClassC c;
    ClassD d;
     // <- test text is inserted here
}
