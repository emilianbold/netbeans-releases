
// IZ#155148: Unresolved namespace alias

namespace N1 {
    int i = 0;
}
namespace N2 {
    namespace N3 = N1;
}
namespace N2 {
    void foo()     {
        N3::i++;
    }
}
int main() {
    N2::foo();
    return 0;
}
