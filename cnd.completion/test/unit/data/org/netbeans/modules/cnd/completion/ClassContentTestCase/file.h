

class D {
public:
    D() {
        //
    }
    virtual ~D() {
        //
    }
};

class E : public D {
public:
    E() {
        //
    }
    //
    virtual ~E() {
        //
    }
};
  
class F {
public:
    F();
    int i;
    class {
    public:
        int j;
        struct {
            int k;
            union {
                int l;
                int m;
            };
        };
        union {
            int n;
            int o;
        };
    };
    class G {
    public:
        int t;
    };
    enum {p};
};
