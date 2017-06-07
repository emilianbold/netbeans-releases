namespace bug269199 {
    struct AAA269199 {
        void foo();
    };

    AAA269199 final();
    AAA269199 override();

    int main269199() {
        final().foo();
        override().foo();
        return 0;
    }
}