struct AA246680 {
    struct BB246680 {
        struct CC246680 {
                int a3;
        } a2[3];
    } a1[3];
};
typedef struct AA246680 A246680;

int main246680() {
    A246680 aa = {.a1[0].a2[0].a3 = 111};
    return 0;
}  