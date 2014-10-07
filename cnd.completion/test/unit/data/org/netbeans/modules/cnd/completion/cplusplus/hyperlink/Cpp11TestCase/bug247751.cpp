namespace bug247751 {
    struct AAA247751 {
        int foo();
    };

    AAA247751 array[3];

    auto foo247751() -> AAA247751 {
        return AAA247751();
    }

    int main247751() {
        auto var1 = AAA247751();
        auto var2 = decltype(AAA247751())();
        for (auto ttt : array) {
            ttt.foo();
        }
    } 
}