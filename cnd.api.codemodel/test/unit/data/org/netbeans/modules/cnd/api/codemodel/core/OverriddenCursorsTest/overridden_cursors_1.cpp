template <int V, typename T>
struct TTT : TTT<V - 1, T> {
    
    T value;
    
    T get() {
        return value;
    }
    
    int boo() {
        return V;
    }
    
};

template <typename T>
struct TTT<0, T> {
    
    T value;
    
    T get() {
        return value;
    }
    
    virtual int boo() {
        return 0;
    }
    
};

struct AAA : TTT<3, int> {
    
    int get() {
        return 0;
    }
    
    int boo() {
        return 0;
    }
    
};
