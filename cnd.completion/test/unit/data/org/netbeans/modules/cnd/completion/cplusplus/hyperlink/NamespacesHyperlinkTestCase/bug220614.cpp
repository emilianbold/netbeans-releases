
namespace {
    void
    bug220614_function1() {
        std::cout << "::function1()\n";
    }
}
namespace bug220614_code {
    void 
    bug220614_function1() {
        std::cout << "code::function1()\n";
    }
    int
    bug220614_function2() {
        bug220614_function1();
        ::bug220614_function1();
        return 0;
    }
}
int
bug220614_main(int argc, char** argv) {
    return bug220614_code::bug220614_function2();
}