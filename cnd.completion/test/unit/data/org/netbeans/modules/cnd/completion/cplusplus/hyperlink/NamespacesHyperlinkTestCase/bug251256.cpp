void b251256(char smth) {}

static void b251256(int smth) {}

namespace {
    void b251256(bool smth) {}
}

static int bug251256() {
    ::b251256('a');
    ::b251256(6);
    ::b251256(true);
    return 0;
}