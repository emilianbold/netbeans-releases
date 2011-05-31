class bug197394_A {
public:
    __extension__ union {
        int i;
    };
};

int bug197394_main(int argc, char** argv) {
    bug197394_A a;
    a.i;
    return 0;
}
