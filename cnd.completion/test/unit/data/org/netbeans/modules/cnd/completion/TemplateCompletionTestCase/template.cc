
template <int i> class T1
{
public:
    int s;
    
    static int foo()
    {
        
    }
    
    int GetI()
    {
        return i;
    }
};

template <class C> class T2
{
public:
    int i;
    static int s;
};

template <int k, class C> class T3
{
public:
    int i;
    static int s;
};


int main() 
{  
    T1<1> t1;
    
     // select<Person>().one().
    
    return 0;
}

template <class T> class T4
{
public:
    T t;
};

class Person{
public:
    void method();
};

template <class T>
class DataSource {
public:
    T one() const {
        T t;
        return t;
    }
};


template <class T> DataSource<T> select() {
    DataSource<T> p;
    return p;
}
