
namespace {
    class A {
    public:
        A(const char*, ...) {
        }
        void b() const {
        }
    };

    void c() {
        A("a" "b", "c").b();
    }
}