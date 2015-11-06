#include <stdio.h>

typedef unsigned long long uintmax_t;

int main() {
    uintmax_t uim_t = 12;
    printf("%jo", uim_t);

    return 0;
}