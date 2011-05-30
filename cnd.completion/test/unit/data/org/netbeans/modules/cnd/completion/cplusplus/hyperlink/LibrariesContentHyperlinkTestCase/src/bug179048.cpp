
void bug179048_foo() {
}

namespace bug179048_std {
    using bug179048_foo;
}

void bug179048_bar() {
    bug179048_std::bug179048_foo();
}