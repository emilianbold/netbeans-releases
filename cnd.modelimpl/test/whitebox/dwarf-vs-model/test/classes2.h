#ifndef _CLASSES2_H_
#define _CLASSES2_H_

class Class2 {
public:
	int m_1() {
		int r = 0;
		return r;
	}
	
	int m_2();
	static int static_field;
};

namespace example {
    class Person {
    public:
	class SexType {
	public:
	    static const int Male;
	};
    };
}


#endif // _CLASSES2_H_
