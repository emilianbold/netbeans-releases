enum bug190127_LD_HELPERS {
        LD_HELP_STAB,
        LD_HELP_ANNOTATE,
        LD_HELP_CCEXCEPT
};

int bug190127_main(int argc, char** argv) {
    static struct _helper_desc {
            const char* name;
            int mode;
    } tab[] = {
            [LD_HELP_STAB] = { .name = "lib1.so", .mode = 32 | 64 },
            [LD_HELP_ANNOTATE] = { .name = "lib2.so", .mode = 32 | 64 },
            [LD_HELP_CCEXCEPT] = { .name = "lib3.so.1", .mode = 32 | 64 },
    };
}
