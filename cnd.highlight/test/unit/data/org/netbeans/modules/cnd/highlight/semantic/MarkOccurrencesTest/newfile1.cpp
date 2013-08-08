namespace std {
    class string {
    public:
        string(const char*){};
    };
    
    template<typename T>
    class vector {
        public:
            vector<T>() {};
    };
}

int bar = 1;
void func(int bar) {}