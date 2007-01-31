#include "classes2.h"

int classes2_foo() {
	Class2 cls2;
	return cls2.m_1();
}

int Class2::m_2() {
	int r = 1;
	return r;
}

int Class2::static_field = 0;

namespace example {
    const int Person::SexType::Male(0);
}

