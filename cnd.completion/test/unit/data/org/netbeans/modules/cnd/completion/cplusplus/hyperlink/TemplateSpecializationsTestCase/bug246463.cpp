namespace bug246463 {
    template <typename T = void>
    struct AAA246463;

    template <>
    struct AAA246463<> {}; // click on AAA navigates to forward declaration
}