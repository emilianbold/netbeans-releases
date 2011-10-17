struct auto_A {    
    int i;
};

auto_A auto_foo() {
    auto_A a;
    return a;
}

auto auto_a2 = auto_foo();   
static auto *auto_a3 = &auto_a2;   

int auto_main() {
    auto_a3->i;
}