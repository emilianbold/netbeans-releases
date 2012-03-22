class bug206220_A{
    friend class bug206220_B;

    int var;
public:
};

class bug206220_B{
public:
    
    class Nested{
    public:
        void func(bug206220_A* a){
            a->var = 10; // var is underlined with red wave line
        }
    };
};