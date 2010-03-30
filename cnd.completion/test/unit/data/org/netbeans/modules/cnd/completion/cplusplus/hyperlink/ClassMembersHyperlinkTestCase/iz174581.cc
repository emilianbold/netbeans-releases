namespace iz174581 {

struct A {
    int i;
};

struct B {
    A& operator*() {
        A a;
        a.i = 1;
        return a;
    }
};

struct C {
    B getB() {
        B b;
        return b;
    }
};

int main() {
    C c;
    (*c.getB()).i;

    return 0;
}

}
