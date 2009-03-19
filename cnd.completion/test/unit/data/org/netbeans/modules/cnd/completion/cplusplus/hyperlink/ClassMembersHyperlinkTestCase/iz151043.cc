class iz151043_C {
public:
    int i;
};

int iz151043_main() {
    iz151043_C **c;
    (*c)->i; // ok
    (*(c))->i; // unresolved
    (*(iz151043_C**)c)->i; // unresolved
    return 0;
}