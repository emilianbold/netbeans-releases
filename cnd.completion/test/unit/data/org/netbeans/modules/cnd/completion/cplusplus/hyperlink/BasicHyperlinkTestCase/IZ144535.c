typedef struct IZ144535_pcihp {
    struct IZ144535_pcihp *nextp;
    struct IZ144535_pcihp_slotinfo {
        char *name;
    } slotinfo[10];
    int bus_flags;
} IZ144535_pcihp_t;

void IZ144535_foo(struct IZ144535_pcihp_slotinfo * x) {
    x->name[0];
}