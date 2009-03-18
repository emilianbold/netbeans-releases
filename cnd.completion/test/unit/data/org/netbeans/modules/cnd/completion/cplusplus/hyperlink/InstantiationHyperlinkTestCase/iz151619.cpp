template <class T> class C {
public:
    typedef T* pT;
};

C<C<int> >::pT pt1; // pT is resolved
C<C<int>*>::pT pt2; // pT is unresolved