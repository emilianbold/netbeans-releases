namespace bug246683 {
    template <class T0, class ...T1> 
    struct A246683 {
        int foo() {
            return 1;
        }
    };

    template <class T> 
    struct A246683<T> {
        int foo() {
            return 0;
        }
        int bar() {
            return 0;
        }
    };

    int main246683() {
        A246683<int,double>().foo();
        A246683<int>().foo()
        A246683<int>().bar();
        return 0;
    }
}