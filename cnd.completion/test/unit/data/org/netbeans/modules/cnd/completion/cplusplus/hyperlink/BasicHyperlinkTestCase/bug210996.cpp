struct bug210996_Interp {
    int bug210996_env;
    // navigation from the 'struct env' goes to the field env
    // and it is colored as field (green)
    struct bug210996_env    *pEnv;
};

struct    bug210996_env {
    int *savefd;
    struct env *oenv;
};