namespace bug256296 {
    struct AAA256296 {
        int field;
    };

    int foo256296() {
        AAA256296 *pointer = 0;
        return ((pointer)++)->field;
    }
}