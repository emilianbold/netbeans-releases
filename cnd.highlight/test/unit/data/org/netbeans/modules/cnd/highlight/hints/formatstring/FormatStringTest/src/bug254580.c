#include <stdio.h>

typedef long size_t;
typedef long* ptrdiff_t;

int main() {
    size_t st = sizeof(int);
    ptrdiff_t pt = 1;
    size_t* pst = &st;
    ptrdiff_t* ppt = &pt;
    printf("%zu %td", st, pt);
    printf("%zn %tn\n", pst, ppt);
    return 0;
}