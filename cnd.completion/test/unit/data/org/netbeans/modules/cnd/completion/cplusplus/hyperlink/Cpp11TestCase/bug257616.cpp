namespace bug257616 {
    template <typename T>
    bool consume257616(T t) {
        return false;
    }

    void foo257616() {
        int x = 0;
        if (consume257616([](int a) { return a; })) {}
        while (consume257616([=](int &b) { return b + x; })) {}
    }
}
