struct bug192967_A {
    struct bug192967_A* next;
    int field;
};

int bug192967_main(int argc, char** argv) {
    struct bug192967_A* obj = (struct bug192967_A*)argv[1];
    ( argc == 1 ? obj : obj->next)->field;
    return 0;
}

typedef struct bug192967_2_A {
    struct bug192967_2_A* next;
    int field;
} bug192967_2_AA;

int bug192967_2_main(int argc, char** argv) {
    struct bug192967_2_A** obj2 = (struct bug192967_2_A*)argv[2];
    struct bug192967_2_A** obj3 = (struct bug192967_2_A*)argv[3];
    int val = 10;
    ( argc == 1 ? obj2 : obj3)[val]->field;
    return 0;
}