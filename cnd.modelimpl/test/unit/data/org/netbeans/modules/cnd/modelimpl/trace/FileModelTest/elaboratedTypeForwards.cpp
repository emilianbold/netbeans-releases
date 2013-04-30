namespace XXX {

    template<class T>
    struct AAA {
        
        T *param;

        int foo(struct BBB * a);

    };
    
    BBB *a;
    
    typedef AAA<struct CCC> AAACCC;
    
    CCC *c;
    
    
    struct BBB {
        int a;
        int c; 
    };
}

    
int main(void) {
    return 0;
}