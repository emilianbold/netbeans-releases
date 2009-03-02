
#define ZERO 0
#define ZERO_O 0
#define eat(x) x

struct In_Macro {
    In_Macro value;
};

void checkZero() {
    int i = ZERO;
    In_Macro mm;
    eat(mm.value);
}

