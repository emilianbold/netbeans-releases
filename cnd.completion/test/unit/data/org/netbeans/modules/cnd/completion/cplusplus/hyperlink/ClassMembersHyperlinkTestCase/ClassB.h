#ifndef __B__H__
#define __B__H__
typedef int ostream;
#include "ClassA.h"

class ClassB : public ClassA {
public:
    enum type { MEDIUM,  HIGH };

    ClassB() {
    }

    ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
    }

    ClassB(int type1, int type2 = HIGH);

    void method(int a);

    void method(const char*);

    void method(char*, double);

    void method(char*, char*);
private:
    int myType1;
    int myType2;

public:
    void* myPtr;
    int myVal;

public:
    void setDescription(const char* description);

    void setDescription(const char* description, const char* vendor, int type, int category, int units);

    void setDescription(const ClassB& obj);

};

#endif 
