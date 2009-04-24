class iz104945_A {
public:
    int method();
    int method(int*);
    int method(const int* const);
    int method(iz104945_A*);
    int method(const iz104945_A* const);
};

int iz104945_A::method() {
    return 0;
}

int iz104945_A::method(int* a) {
    return 0;
}

int iz104945_A::method(const int* const a) {
    return 0;
}

int iz104945_A::method(iz104945_A* a) {
    return 0;
}

int iz104945_A::method(const iz104945_A* const a) {
    return 0;
}