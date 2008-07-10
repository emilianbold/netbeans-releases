
// IZ 138216 : IDE highlights typedef typename ct_imp2<T, ... line as wrong
template <typename T, bool small_>
struct ct_imp2
{
   typedef const T& param_type;
};

template <typename T, bool isp>
struct TT {
    typedef typename ct_imp2<T, sizeof (T) <= sizeof (void*) >::param_type param_type;
};

// IZ 139360 : parser fails on "a->f<A>()"
class A {
public:
    template<class T> void f();
};

int main(int argc, char** argv) {
    A* a = new A();
    a->f<A>(); // ERROR: unexpected token )(RPAREN)
    (*a).f<A>(); // no error
    delete a;
}

// IZ 139358 : parser fails on "static_cast<int* (*)(int)>"
int main2(int argc, char** argv) {
    void *a = 0;
    static_cast<int* (*)(int)>(a); // ERROR: expecting RPAREN, found 'int'
}


