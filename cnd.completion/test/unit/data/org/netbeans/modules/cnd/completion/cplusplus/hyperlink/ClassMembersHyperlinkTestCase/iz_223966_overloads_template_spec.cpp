template <class T> class complex {    
};

template<> class complex<long> {    
};

int abs(int x) { return (x >= 0) ? x : -x; }
double abs(double x) { return (x >= 0) ? x : -x; }
template <class T> complex<T> abs (const complex<T>& x) { return x; }

int main(int argc, char** argv) {
    complex<int> ci;
    complex<long> cl;
    abs(ci);
    abs(cl);
    return 0;
}
