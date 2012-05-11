namespace ns {
    struct Global {
        void zoo();
    };
}
 
typedef ns::Global Global;
int main() {
    Global b;
    b.zoo();
    
    ns::Global g;
    g.zoo();
    
    struct Global {
        void zzzz() {
            
        }
    };
        
    Global zz;
    zz.zzzz(); 
}
