#include <stdio.h>

enum {
    true = 1,
    false = 0
};

#if TRACE
#define trace(args...) { fprintf(stderr, "!RFS> "); fprintf(stderr, ## args); fflush(stderr); }
#else
#define trace(...) 
#endif
