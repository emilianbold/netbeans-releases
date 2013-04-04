
template<typename T>
class bug223934_A {
    T t;
    
    public:
        void f() {
            std::cout << t << std::endl;
        }
};

template<>
class bug223934_A<int> {
    int t; 

    public:
        void f() {
            std::cout << t << std::endl;
        }    
};

template<>
class bug223934_A<double> {
    double t; 
    
    public:
        void f() {
            std::cout << t << std::endl;
        }        
};


int main() {
    bug223934_A<int> a;
    bug
    
    return 0;
}