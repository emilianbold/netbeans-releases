
int foo() {
    int i = 0;
    __extension__ ({i++;});
    (void) __extension__ ({i--;});
    
    return i;
}