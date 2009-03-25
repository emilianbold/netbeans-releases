struct iz151763_C {
    int i;
    iz151763_C operator() () {
        iz151763_C c;
        return c;
    }
};

struct iz151763_A {
    iz151763_C c;
};

int iz151763_main() {
    iz151763_C c;
    c().i;

    iz151763_A a;
    a.c().i;

    return 0;
}

