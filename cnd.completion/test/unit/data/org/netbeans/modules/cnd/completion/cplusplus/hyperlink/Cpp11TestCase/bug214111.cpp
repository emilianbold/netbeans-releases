class MyQString
{
public:
    MyQString &append(char* c);
};

template <typename T>
class MyQList
{
public:

    struct MyNode { void *v;
        T &t()
        { return v ; }
    };
    
    class const_iterator;

    class const_iterator {
    public:
        MyNode *i;
        
        inline const_iterator() : i(0) {}
        inline const_iterator(MyNode *n) : i(n) {}
        inline const_iterator(const const_iterator &o): i(o.i) {}

        inline const T &operator*() const { return i->t(); }
        inline const T *operator->() const { return &i->t(); }

        inline const_iterator &operator++() { ++i; return *this; }
        inline const_iterator operator++(int) { MyNode *n = i; ++i; return n; }
    };
    friend class const_iterator;

    // stl style
    inline const_iterator constBegin() const {}
    inline const_iterator constEnd() const {}

};

class MyQStringList : public MyQList<MyQString>
{
};

void bug214111_foo() {
    const MyQStringList& lobuf;
    for (auto&& Loit = lobuf.constBegin();Loit != lobuf.constEnd();Loit++) {
             Loit->append("a");
    } 
}