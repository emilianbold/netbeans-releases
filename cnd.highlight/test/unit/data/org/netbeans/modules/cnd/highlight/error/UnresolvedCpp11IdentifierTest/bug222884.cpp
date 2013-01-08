
int bug222884_main() {
    int i1 = 0;
    int i2 = 0;
    try {
        throw i1, ++i2;
    } catch (int i) {
    }
    return 0;
}
