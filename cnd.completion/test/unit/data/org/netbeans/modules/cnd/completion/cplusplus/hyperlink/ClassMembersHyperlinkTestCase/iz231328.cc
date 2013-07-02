class hello231328
{
public:
    int x;
    int y;
    int z;
    hello231328* (*func)(void);
    hello231328(int px, int py, int pz, void (*f)()) {
        x = px;
        y = py;
        z = pz;
        func = f;
    }
};

hello231328* foo231328()
{
    printf("hello world!");
}

template<typename _Tp> class vector231328 {
public:
    typedef _Tp   value_type;   
    typedef _Tp&  reference;
    void push_back(const value_type& __x) {
    }
    
    reference operator[](int __n) {
        return 0;
    }    
};

int main231328() 
{
    vector231328<hello231328> h;
    h.push_back(hello231328(0,0,0,foo231328));
    h[0].func()->x;
    return 0;
}
