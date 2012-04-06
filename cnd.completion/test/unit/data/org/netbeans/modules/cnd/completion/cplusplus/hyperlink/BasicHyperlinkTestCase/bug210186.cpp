
class bug210186_b {
    bool t;
    bug210186_b(bool _t): t (_t) { }
};
void bug210186_foo() {
    bug210186_b a = { false }; // unresolved a
}