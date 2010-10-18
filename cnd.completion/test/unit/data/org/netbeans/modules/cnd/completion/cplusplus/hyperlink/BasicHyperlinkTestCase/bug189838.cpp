class bug189838_TestClass{
    int a;
public:
    bug189838_TestClass(int b){}

    void Func(){}
};


int bug189838_main(int argc, char** argv) {

    bug189838_TestClass(2*4).Func();//c++ parser complains about Func()

    return 0;
}