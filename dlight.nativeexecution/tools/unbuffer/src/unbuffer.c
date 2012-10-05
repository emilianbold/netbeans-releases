#include <stdio.h>
static void __attribute__((constructor)) lib_init(void);

static void lib_init(void) {
    setbuf(stdout, NULL);
}
