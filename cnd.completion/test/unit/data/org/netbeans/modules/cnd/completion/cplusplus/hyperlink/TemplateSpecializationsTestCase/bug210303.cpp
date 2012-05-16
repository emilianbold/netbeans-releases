template <class T> class bug210303_A {    
public:
    int i;
};

void bug210303_foo() {
    bug210303_A<int> a;
    a.i++;
}