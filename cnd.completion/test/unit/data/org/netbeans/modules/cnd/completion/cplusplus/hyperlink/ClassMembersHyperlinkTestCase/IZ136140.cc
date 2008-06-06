template<typename T> struct List_iterator {
    typedef T* pointer;
    typedef T& reference;
    reference operator*() const { return node; }
    pointer operator->() const { return node; }
private:
    reference node;
};

struct Cust {
    int foo();
};

int main() {
    List_iterator<Cust> i2;
    i2->foo();
    (*i2).foo();
}
