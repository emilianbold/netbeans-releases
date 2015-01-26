
struct AAA {
    
    virtual int foo() {
        return 0;
    }
    
};

struct BBB : AAA {
    
    int foo() {
        return 1;
    }
    
};
