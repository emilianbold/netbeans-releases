
#include "ClassA.h" // in test
// members
/*static*/ int ClassA::publicMemberStInt = 1;
/*static*/ int ClassA::protectedMemberStInt = 2;
/*static*/ int ClassA::privateMemberStInt = 3;
    
ClassA::ClassA() : privateMemberInt(1) { // in test testConstructors
    
}

ClassA::ClassA(int a) { // in test testConstructors
    
}

ClassA::ClassA(int a, double b) { // in test testConstructors
    
}

ClassA::~ClassA() { // in test testDestructors
    
}

void ClassA::publicFoo() { // in test testPublicMethods
    
}
void ClassA::publicFoo(int a) { // in test testPublicMethods
}

void ClassA::publicFoo(int a, double b) { // in test testPublicMethods
}

void ClassA::publicFoo(ClassA a) { // !!!FAILED!!!
}

void ClassA::publicFoo(const ClassA &a) { // !!!FAILED!!!
}

/*static*/ void ClassA::publicFooSt() {  // in test testPublicMethods
}

void ClassA::protectedFoo() {  // in test testProtectedMethods    
}

void ClassA::protectedFoo(int a) {      // in test testProtectedMethods
}

void ClassA::protectedFoo(int a, double b) {  // in test testProtectedMethods    
}

void ClassA::protectedFoo(const ClassA* const ar[]) {    // !!!FAILED!!!  
}

/*static*/ void ClassA::protectedFooSt() {  // in test testProtectedMethods     
}

void ClassA::privateFoo() {      // in test testPrivateMethods
}

void ClassA::privateFoo(int a) {      // in test testPrivateMethods
}

void ClassA::privateFoo(int a, double b) {      // in test testPrivateMethods
}

void ClassA::privateFoo(const ClassA *a) {      // in test testPrivateMethods
}

/*static*/ void ClassA::privateFooSt() {      // in test testPrivateMethods
}

////////////
// operators
ClassA& ClassA::operator= (const ClassA& obj) { // in test testOperators
    return *this;
}

ClassA& ClassA::operator+ (const ClassA& obj) { // in test testOperators
    return *this;
}

ClassA& ClassA::operator- (const ClassA& obj) { // in test testOperators
    return *this;
}

ClassA* ClassA::classMethodRetClassAPtr() {
    return this;
}

const ClassA& ClassA::classMethodRetClassARef() {
    return this;
}

myInt ClassA::classMethodRetMyInt() {
    return 0;
}

myInnerInt ClassA::classMethodRetMyInnerInt() {
    return 0;
}

ostream& operator <<(ostream& output, const ClassA& item) {
    output << item.privateMemberInt << customer.privateMemberDbl;
    return output;
}

void friendFoo() {

}
