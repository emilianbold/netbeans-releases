namespace bug235229 {
    void foo_235229(int param = [](int a){return a;}(10)) {}
}