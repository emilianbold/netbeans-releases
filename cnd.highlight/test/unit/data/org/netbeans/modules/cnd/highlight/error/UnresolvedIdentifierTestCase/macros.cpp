#define MMM(x) x
#define CONCAT(a, b, c) a##b##c
namespace std {}

int main() {
    MMM(std)::cout << "Hello";
    CONCAT(s, t, d)::cout << endl;
}
