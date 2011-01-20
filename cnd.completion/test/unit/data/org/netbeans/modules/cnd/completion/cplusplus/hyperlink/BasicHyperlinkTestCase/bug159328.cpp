template<class T>
struct bug159328_A {
    bug159328_A(int) {
    }
    int bar() {
    }
};
void bug159328_foo() {
    static_cast< bug159328_A<int> >(0).bar();
}