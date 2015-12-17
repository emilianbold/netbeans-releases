#include <stdio.h>
typedef long size_t;
typedef int* ptrdiff_t;

int main(int argc, char** argv) {
    size_t s = sizeof(int);
    printf("%zu", s);
    
    int x, y;
    int *p1 = &x;
    int *p2 = &y;
    ptrdiff_t diff = p2 - p1;
    printf("%td\n", diff);
    
    return 0;
}