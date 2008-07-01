
int foo() {
    int i = 0;
    __extension__ ({i++;});
    (void) __extension__ ({i--;});
    
    ({i++;});
    (void) ({i--;});
    
    return i;
}