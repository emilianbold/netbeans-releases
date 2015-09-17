namespace bug244177_2 {
    struct AAA244177_2 {
        int foo();
    };

    template <typename T>
    T boo244177_2();

    decltype(boo244177_2<decltype(AAA244177_2())>()) x244177_2;
}