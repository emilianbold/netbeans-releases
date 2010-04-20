class iz142674_c {
public:
    iz142674_c(): i(0), j(0) {}
    iz142674_c(const int i): i(i), j(i) {};
    ~iz142674_c() {};

    // setters
    iz142674_c& seti(const int i = 0) { this->i = i; return *this; };
    iz142674_c& setj(const int j = 0) { this->j = j; return *this; };
private:
    int i, j;
};

int iz142674_main(int argc, char** argv) {
    iz142674_c* c2 = new iz142674_c(0);
    bool b(false);
    int i(1);

    c2->seti(i).setj(i); // ok
    c2->seti((b?i:-1)).setj((b?i:-1)); // ok
    c2->seti((b?i:-1)).setj((b?-1:i)); // ok
    c2->seti((b?-1:i)).setj((b?i:-1)); // error, unresolved setj

    return EXIT_SUCCESS;
}