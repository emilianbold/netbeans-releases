template<int P, typename T, class U> struct List_iterator {
    typedef T* pointer;
    typedef T& reference;
    reference operator*() const { return node; }
    pointer operator->() const { return node; }
    List_iterator(T);
    create(U,T){ int i = P;}
    int s[P];
private:
    reference node;
};
