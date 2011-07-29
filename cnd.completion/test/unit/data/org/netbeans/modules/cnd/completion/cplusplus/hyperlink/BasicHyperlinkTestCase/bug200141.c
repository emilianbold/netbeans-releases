typedef struct bug200141__A{
    int v;
} bug200141_A;
typedef struct bug200141__B{
    char *    a;
    void *    b;
} bug200141_B;
bug200141_B t[] = {
    { .a = "s", .b = & (bug200141_A) { .v = 1}  },
    { .a = "d", .b = & (bug200141_A) { .v = 1 } },
    { .a = "e", .b = & (bug200141_A) { .v = 1 } },
    { .a = 0 }
};