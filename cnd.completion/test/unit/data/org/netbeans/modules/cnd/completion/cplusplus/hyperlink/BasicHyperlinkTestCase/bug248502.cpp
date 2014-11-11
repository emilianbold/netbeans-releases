namespace bug248502 {
    struct W248502 {
        void func();
    };

    W248502* f_248502(int var);
    int f1_248502(int var);

    template <int Val>
    struct Holder248502 {
        static const int value = Val;
    };

    template <int Val>
    struct Data248502 {
        int operator-(int other);
        int operator+(int other);
        int operator*(int other);
        int operator&(int other);
    };

    int main248502() {
        int i = 1;  
        f_248502(i + 6)->func(); //ok 
        f_248502(i * 6)->func(); //ok 
        f_248502(i * (6))->func(); //ok 
        f_248502(i * -6)->func(); //ok 
        f_248502(i - 6)->func(); //ok 
        f_248502(i & 6)->func(); //ok 
        f_248502(i & (6))->func(); //ok 
        f_248502(i & -6)->func(); //ok         
        f_248502(Holder248502<6>::value + 6)->func(); //ok 
        f_248502(Holder248502<6>::value * 6)->func(); //ok 
        f_248502(Holder248502<6>::value * (6))->func(); //ok 
        f_248502(Holder248502<6>::value * -6)->func(); //ok        
        f_248502(Holder248502<6>::value - 6)->func(); //ok 
        f_248502(Holder248502<6>::value & 6)->func(); //ok 
        f_248502(Holder248502<6>::value & (6))->func(); //ok 
        f_248502(Holder248502<6>::value & -6)->func(); //ok             
        f_248502(f1_248502(3) + 6)->func(); //ok 
        f_248502(f1_248502(3) * 6)->func(); //ok 
        f_248502(f1_248502(3) * (6))->func(); //ok  
        f_248502(f1_248502(3) * -6)->func(); //ok 
        f_248502(f1_248502(3) - 6)->func(); //ok 
        f_248502(f1_248502(3) & 6)->func(); //ok 
        f_248502(f1_248502(3) & (6))->func(); //ok  
        f_248502(f1_248502(3) & -6)->func(); //ok     
        f_248502(Data248502<5>() + 6)->func(); //ok 
        f_248502(Data248502<5>() * 6)->func(); //ok 
        f_248502(Data248502<5>() * (6))->func(); //ok  
        f_248502(Data248502<5>() * -6)->func(); //ok         
        f_248502(Data248502<5>() - 6)->func(); //ok 
        f_248502(Data248502<5>() & 6)->func(); //ok 
        f_248502(Data248502<5>() & (6))->func(); //ok  
        f_248502(Data248502<5>() & -6)->func(); //ok             
    }
}