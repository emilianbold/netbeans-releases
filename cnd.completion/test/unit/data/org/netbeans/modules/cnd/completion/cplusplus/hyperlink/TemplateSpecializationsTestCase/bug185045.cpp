template <class X,class Y>
class bug185045_A{
public:
    int open(int t);
};

template <> int bug185045_A<int,int>::open(int t){
}

int bug185045_main(){
   bug185045_A<int,int> a;
   a.open(1);
}