
// IZ#154112: Unresolved instantiations of template

template <int t>
struct A
{
    typedef int type;
};

template <class T>
struct B
{
    typedef T type;
};

int main() {
    A<1>::type i1; 
    A<-1>::type i2; 
    B< A<1> >::type i3; 
    B< ::A<1> >::type i4; 
}