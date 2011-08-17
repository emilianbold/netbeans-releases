typedef struct bug200115_a {
    struct bug200115_c *i, *j;
} bug200115_aa;
typedef struct bug200115_b {
    union bug200115_d *k,*l;
} bug200115_bb;
int bug200115_main(int argc, char** argv) {   
    long long z;
    bug200115_aa *x;
    bug200115_bb *y;
    &((z + 1) & z ? x : (bug200115_aa *)y->k)->i;
    return 0;
}