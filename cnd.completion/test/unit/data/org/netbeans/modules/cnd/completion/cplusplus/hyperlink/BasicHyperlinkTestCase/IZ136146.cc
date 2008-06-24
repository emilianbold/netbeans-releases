template<typename T> struct List_iterator {
    typedef T* pointer;
    typedef T& reference;
    reference operator*() const { return node; }
    pointer operator->() const { return &node; }
private:
    reference node;
};

template<typename T> struct List {
    typedef List_iterator<T> iterator;
};

struct Cust {
    int foo();
};

int main146() {
    List<Cust>::iterator i1;
    i1->foo();
    (*i1).foo();
}
