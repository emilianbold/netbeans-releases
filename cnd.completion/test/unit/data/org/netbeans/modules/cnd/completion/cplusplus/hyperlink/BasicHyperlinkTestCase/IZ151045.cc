class iz151045_C {
public:
    int i;
};

#define CLASS_C iz151045_C

int iz151045_main() {
    C *c;
    ((C*)c)->i; // ok
    ((CLASS_C *)c)->i; // unresolved

    return 0;
}