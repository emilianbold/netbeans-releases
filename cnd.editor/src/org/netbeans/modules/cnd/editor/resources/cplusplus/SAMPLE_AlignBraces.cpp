class ClassA : InterfaceA, InterfaceB, InterfaceC {
public:
int number = 1;
private:
String letters[] = new String[]{ "A", "B", "C", "D" };
ClassA() {
}
public:
int method(String text,
int number, Object object) throw ExceptionA, ExceptionB {
printf(nuber + text.length() < 20 ? "message1" : "message2",
       25);
if ( text == null ) {
text = "a";
}
else if (text.length() == 0) {
text = number == 2 ? "empty" : "nonempty";
number = ((op3() + 2) * op4);
}
else {
    number++;
}
for( int i = 1; i < 100; i++ ) {
}
while ( this->number < 2 && number != 3 ) {
method( "Some text", 12,
        new Object());
}
do {
try {
op1().op2.op3().op4();
}
catch ( Throwable t ) {
log();
}
} while ( this->number < 2 && number != 3 );
switch(number) {
case 1:
    return method("text", 22);
case 2:
    return 20;
default:
    return -1;
}
}
enum Where {
NORTH, EAST, SOUTH, WEST;
};
};
