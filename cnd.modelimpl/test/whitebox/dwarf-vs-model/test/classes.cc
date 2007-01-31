struct Point {
	int x;
	int y;
};


class ClassWithInlineMethods {
public:
    int foo() {
	int res = 0;
	for(int x = 0; x < 7; x++ ) {
	    int y = res + x;
	    res += y;
	}
	return res;
    }
};

int foo() {
	Point p;
	ClassWithInlineMethods cls;
	return p.x + p.y + cls.foo();
}
