static union {
    int foo, bar;
    long foobar;
};

int main() {
    union {
        int value;
        char ch[4];
    } b;
    value = 12345;
    ch[0] = bar + b.ch[3];
    return 0;
}
