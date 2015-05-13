void b251256(char smth) {}

static void b251256(int smth) {}

namespace {
    void b251256(bool smth) {}
}

static int bug251256() {
     // void b251256(...)     
    return 0;
}