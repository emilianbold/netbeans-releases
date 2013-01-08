struct bug223298_2_B {
    int      step;
};

uint bug223298_2_A() const  { return 1; }
void bug223298_2_A(uint v) { }

void main () {
    bug223298_2_B c;
    bug223298_2_A(c.step < 1);
}
