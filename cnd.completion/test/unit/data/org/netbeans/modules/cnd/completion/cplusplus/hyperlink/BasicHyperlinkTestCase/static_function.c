
static void static_foo1(){
}

typedef void (*pf_Static) ();

struct C_Static {
    static pf_Static f;
};

pf_Static C_Static::f = static_foo1; // unresolved

struct S_Static {
    void (*f)();
};

static void static_foo2(){
}

struct CC_Static {
    static S_Static s;
};

S_Static CC_Static::s =
{
    static_foo2 // unresolved
};

int main() {
    CC_Static::s.f();
    C_Static::f();
    return 0;
}