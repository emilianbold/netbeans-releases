#include <Map>
/*
 */
namespace A{
class ClassA : InterfaceA, InterfaceB, InterfaceC {
public:
int number;
enum inner {
    PLUS, MINUS
};
private:
char** cc;
public:
ClassA():cc({ "A", "B", "C", "D"}), number(2){
}
int method(char* text, int number) {
if ( text == NULL ) {
    text = "a";
#ifdef C
#define C1
#ifdef F
#define F2
#endif
#else
#define C2
#endif
label:
switch(number) {
case 1:
    return method("text", 22);
case 2:
    return 20;
default:
    return -1;
}
}
else if (text[0] == 0) {
    text = "empty";
}
else {
    number++;
}
}
};
}