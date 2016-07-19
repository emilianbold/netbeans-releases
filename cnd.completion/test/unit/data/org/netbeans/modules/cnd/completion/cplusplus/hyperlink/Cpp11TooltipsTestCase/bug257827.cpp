namespace bug257827 {
    template <typename T>
    struct AAA257827 {};

    struct BBB257827 {};

    void foo257827() {
        AAA257827<const bug257827::BBB257827> var1;
        AAA257827<volatile BBB257827 *> var2;
        AAA257827<const volatile BBB257827> var3;
    }
}