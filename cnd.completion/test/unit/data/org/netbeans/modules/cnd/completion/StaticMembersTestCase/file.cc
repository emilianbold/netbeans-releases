
#include "file.h"
 
void ClassA::aPubFun() {
    ClassA a;
    ClassB b;
    ClassC c;
    ClassD d;
    ClassE e;
     //  <- test text is inserted here
}

void ClassB::bProtFun() {
    ClassA a;
    ClassB b;
    ClassC c;
    ClassD d;
    ClassE e;
     //  <- test text is inserted here
}

void ClassC::cPrivFun() {
    ClassA a;
    ClassB b;
    ClassC c;
    ClassD d;
    ClassE e;
     //  <- test text is inserted here
}

void ClassD::dPubFun() {
    ::ClassA a; // ClassA is inaccessible whithin this context... (?)
    ClassB b;
    ClassC c;
    ClassD d;
    ClassE e;
     //  <- test text is inserted here
}

void ClassE::ePubFun() {
    ClassA a;
    ClassB b;
    ClassC c;
    ClassD d;
    ClassE e;
     //  <- test text is inserted here
}

void friendOfB() {
    ClassA a;
    ClassB b;
    ClassC c;
    ClassD d;
    ClassE e;
     //  <- test text is inserted here
}

