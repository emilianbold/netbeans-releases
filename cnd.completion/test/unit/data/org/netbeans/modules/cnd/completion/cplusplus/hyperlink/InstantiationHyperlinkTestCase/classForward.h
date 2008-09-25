namespace IZ144869 {
template <class T> class allocator {
public:
    typedef T&         reference;
};

template <class T, class Allocator = allocator<T> > class list {
public:
    class iterator;
    class iterator {
        T& operator* () const { return 0;}
    };
};

struct A {
    void foo() const;
};

void main() {
    list<A>::iterator it;
    (*it).foo();
}
}