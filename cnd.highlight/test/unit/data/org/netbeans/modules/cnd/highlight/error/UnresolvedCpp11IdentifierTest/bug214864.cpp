int main(int argc, char** argv) {
    struct C {
      constexpr int f() { return 0; }
    } constexpr c = C();

    return 0;
}