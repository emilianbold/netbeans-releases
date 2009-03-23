class iz151588_C {
public:
    int i;
};

iz151588_C iz151588_m[10];

int iz151588_main() {
    iz151588_m[1].i; // ok
    iz151588_m[((int)' ')].i; // ok
    iz151588_m[(int)' '].i; // unresolved
    return 0;
}