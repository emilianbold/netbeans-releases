template<typename T> struct X0 {
        template<typename U> void g(U);
};
template<> template<> void X0<int>::g(int) { 
}