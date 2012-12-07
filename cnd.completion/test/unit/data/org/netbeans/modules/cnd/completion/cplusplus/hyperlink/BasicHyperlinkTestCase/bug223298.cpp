struct bug223298_B {
    int      step;
};

uint bug223298_A() const  { return 1; }
void bug223298_A(uint v) { }

void main () {
    bug223298_B c;
    bug223298_A(c.step < 1);
}
