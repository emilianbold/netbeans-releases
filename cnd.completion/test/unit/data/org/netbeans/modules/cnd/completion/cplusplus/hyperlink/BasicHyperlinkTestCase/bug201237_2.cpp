class bug201237_2_A {
public:
    bool foo(){
        return true;
    }
    bug201237_2_A* bar(bool){
        return (bug201237_2_A*)0;
    }
    void bar(int) {
    }
};

int bug201237_2_main(int argc, char** argv) {
    bug201237_2_A f;
    f.bar( f.foo() ? f.foo() : f.foo() )->foo();
    f.bar(true)->foo();
    f.bar(10);
    return 0;
}
