
template <typename T> class C2 {
    template<class TT, int j> int A();
    int B();
};

class D2 {
    template<class TT> int A();
    int B();
};

template<class P>
template<typename PP, int k>
int
C2<P>::A() {
    PP p;
    return 0;
}

template<class P>
int
C2<P>::B() {
    return 0;
}

int
D2::B() {
    return 0;
}

template<class PP>
int
D2::A() {
    return 0;
}

int main(){
    return 0; 
}