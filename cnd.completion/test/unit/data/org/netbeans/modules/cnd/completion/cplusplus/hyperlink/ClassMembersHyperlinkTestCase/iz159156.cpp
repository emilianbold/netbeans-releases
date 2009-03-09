struct AAAA {
    short iiii;
};

namespace NNNN {
    class type {
    };
}

namespace NNNN {

    class CCCC {
    public:
        typedef AAAA type;

        void foo() {
            type mr;
            mr.iiii++; // unresolved
        }
    };
}
    
int main() {
    NNNN::CCCC c;
    c.foo();
    return 0;
}
