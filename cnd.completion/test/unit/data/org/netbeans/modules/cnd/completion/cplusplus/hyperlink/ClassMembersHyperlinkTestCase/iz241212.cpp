namespace NS241212 {

class F;
class D;

class A {
public:        
        void AddRef(F *) {}
        void AddRef(D *) {}
        
        static void AddRefStatic(F *) {}
        static void AddRefStatic(D *) {}
};

class D {
        void d() {
                A *a;
                a->AddRef(this)
                A::AddRefStatic(this);
        }
};

}