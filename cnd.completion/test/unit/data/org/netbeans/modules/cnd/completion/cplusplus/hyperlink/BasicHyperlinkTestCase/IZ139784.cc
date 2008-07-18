class IZ139784 {
    enum { E1 = 1, E2 = 2 };
    enum { E3 = E1 + 1, E4 = E2 + 1 };
    char buf1[E1];
    char buf2[E2];
    char buf3[E3];
    char buf4[E4];
};
