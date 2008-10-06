
#include "file.h"

int globInt;

void A::f() {
    
    char* str = "string";
    
    return;
}

void A::f2() {
    char c = ' ';
    return;
}

void globFoo() {
    
    int jOuter = 2;
    switch(j) {
        case 1: {
            char jInComopound=3;
            
        }
        case 2:
            char jNonCompound = 0;
            
            break;
        default:
            char jDeafult=4;
            
    }
}

int main(int argc, char** argv) {
    int value, *pointer;
    value = 0; pointer = &value;
     // <- test text is inserted here
    printf("%d\n", *pointer);
    void* pExtra;
    A a;
     // code completion tests insert some code here
    for (int i = 0; i < 10; i++) {
        int yyy = argc>0 ? static_cast<int>(pExtra) : *pointer;
    }
    return (0);
}
