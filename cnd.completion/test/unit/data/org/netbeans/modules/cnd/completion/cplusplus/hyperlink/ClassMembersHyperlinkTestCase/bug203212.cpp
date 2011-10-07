class bug203212_host;

namespace bug203212_nm01 {
    class wrapper {
    private:
        bug203212_host *h;
    public:
        wrapper(bug203212_host *_h) : h(_h) { }

        void execute01();
    };
};

class bug203212_host {
public:
    host() { }
    void action01() { }
    void action02() { }
} bug203212_the_host;

void
bug203212_nm01::wrapper::execute01() {
    h->action01();
    h->action02();
}

int bug203212_main(int argc, char** argv) {
    bug203212_nm01::wrapper(&bug203212_the_host).execute01();
    return 0;
}