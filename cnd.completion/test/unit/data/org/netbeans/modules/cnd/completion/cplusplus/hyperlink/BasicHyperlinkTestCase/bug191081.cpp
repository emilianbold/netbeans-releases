typedef struct bug191081_F{
        int bits;
} bug191081_f_t;

bug191081_f_t
fset_init()
{
    return (bug191081_f_t) { .bits = 0};
}
