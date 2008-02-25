#include <Map>
/*
 */
struct ClassA
{
    int number;
    static char** cc;
};
ClassA::cc = { "A", "B", "C", "D" };

int method(char* text, int number)
{
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
    }
    else if (text[0] == 0) {
        text = "empty";
    }
    else {
        number++;
    }
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

