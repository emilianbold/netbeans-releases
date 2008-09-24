
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
    
     //
    
    return 0;
}

template <class T> class T4
{
public:
    T t;
};
