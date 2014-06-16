namespace bug244177 {
    struct AAA244177 {
        int foo244177();
    };

    template <typename T>
    T boo244177();

    decltype(boo244177<decltype(AAA244177())>()) x244177;

    int main244177() {
        x244177.foo244177();
    }
}