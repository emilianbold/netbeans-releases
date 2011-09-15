class bug201237_2_A {
public:
    bool foo(){
        return true;
    }
    bug201237_2_A* bar(bool){
        return (bug201237_2_A*)0;
    }
};

int bug201237_2_main(int argc, char** argv) {
    bug201237_2_A f;
    f.bar( f.foo() ? f.foo() : f.foo() )->foo();
    
    return 0;
}
