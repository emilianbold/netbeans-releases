namespace mystd153986 {
    class string {
    public:
        void mmmm();
    };
    template <class T> class vector {
    public:
        T method();
        T push_back(T t);
    };
}
namespace MYSTL153986 {
    using namespace mystd153986;
}

void test153986(){
    MYSTL153986::vector<MYSTL153986::string> vec2;
    vec2.push_back("str2").mmmm();
}
