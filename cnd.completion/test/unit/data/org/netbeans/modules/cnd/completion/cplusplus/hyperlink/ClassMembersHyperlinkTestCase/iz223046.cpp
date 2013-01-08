namespace std {
    template <class T> class vector {
    };
}

struct strA {
    int par;
};

struct strB {
    char par;
};

class testing {
public:
    virtual void function(const std::vector<strB> &arg1, int arg2, int arg3, const bool arg4) {};
    virtual void function(const std::vector<strA> &arg1, int arg2, int arg3, const bool arg4) {};
    void foo(int);
};


void testing::foo(int argc) {

    std::vector<strA> vec_a;
    std::vector<strB> vec_b;
    testing the_testing;

    if(argc == 2) {
        function(vec_a, 0, 1, false);
        this->function(vec_a, 0, 1, false);
        the_testing.function(vec_a, 0, 1, false);
    } else {
        function(vec_b, 1, 2, true);
        this->function(vec_b, 1, 2, true);
        the_testing.function(vec_b, 1, 2, true);
    }
}
