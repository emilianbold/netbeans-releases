#define EMPTY

void iz151705_foo(int i) {
}

int iz151705_k = 0;

int iz151705_main() {
    foo(EMPTY iz151705_k); // unresolved k
    return 0;
}