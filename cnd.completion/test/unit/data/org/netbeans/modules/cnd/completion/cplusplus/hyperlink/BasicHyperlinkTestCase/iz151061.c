struct iz151061BBB {
    int field;
};

struct iz151061BBB a[] = {
    { .field = 1}, // unresolved
    { .field = 2}, // unresolved
};

const struct iz151061BBB* iz151061foo() {

    struct iz151061AAA {
        int field1;
    };

    struct iz151061AAA a[] = {
        { .field1 = 1}, // unresolved
        { .field1 = 2}, // unresolved
    };

    for (const struct iz151061AAA* res; ;) {
        res->field1;
    }
    iz151061foo()->field;
}
