template<typename T> struct List_iterator2 {
    typedef T* pointer;
    typedef T& reference;
    reference operator*() const { return node; }
    pointer operator->() const { return &node; }
private:
    reference node;
};

template<typename T> struct List2 {
    typedef List_iterator2<T> iterator;
};

struct Cust2 {
    int foo();
};

int main146() {
    List2<Cust2>::iterator i1;
    i1->foo();
    (*i1).foo();
}
