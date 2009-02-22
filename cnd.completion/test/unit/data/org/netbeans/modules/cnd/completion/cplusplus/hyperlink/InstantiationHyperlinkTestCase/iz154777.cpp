namespace iz154777 {
    struct BB {
        void method();
    };
    template <class T> struct CC {
    };

    template <> struct CC<int> {

        struct DD {
            typedef BB dType;
        };
    };

    int main() {
        CC<int>::DD::dType j; // unresolved DD
        j.method();
    }
}