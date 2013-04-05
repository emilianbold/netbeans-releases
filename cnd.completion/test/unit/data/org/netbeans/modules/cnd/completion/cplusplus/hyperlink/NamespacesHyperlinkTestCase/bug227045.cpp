
namespace {

class a_class {
public:
    bool operator()() const { return true; }

    static a_class instance01;
    static a_class instance02;
};

a_class a_class::instance01;
a_class a_class::instance02;

}

int main(int argc, char** argv) {

    bool result = ::a_class::instance01(); // (1)

    return 0;
}