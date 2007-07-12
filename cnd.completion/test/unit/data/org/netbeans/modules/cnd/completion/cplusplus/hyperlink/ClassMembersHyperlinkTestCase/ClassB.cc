

#include "ClassB.h"

ClassB::ClassB(int type1, int type2 /* = HIGH*/) :
ClassA(type1), myType2(type2), myType1(MEDIUM)
{
    method("string");
    method("string", "string");
}

