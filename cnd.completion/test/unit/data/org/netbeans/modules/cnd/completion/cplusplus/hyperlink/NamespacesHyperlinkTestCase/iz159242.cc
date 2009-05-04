namespace iz159242_N {

    template<typename T>
    class singleton {
    public:
        static T& instance() {
            static T the_inst;
            return the_inst;
        }
    protected:
        singleton() {
        }
        ~singleton() {
        }
    };

    class A : public singleton<A> {
    public:
        int i;
    };

    namespace {
    namespace {
        A& a = A::instance();
    }
    }
}


void
iz159242_foo() {
    using namespace iz159242_N;
    a.i++;
}
