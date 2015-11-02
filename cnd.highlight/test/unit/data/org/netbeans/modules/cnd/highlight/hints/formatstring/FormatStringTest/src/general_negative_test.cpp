#include <stdio.h>

int main(int argc, char** argv) {
    short int si = -2;
    float f = 12.8f;
    int i = -1;
    
    // Errors
    printf("No args", f);
    printf("%f", f, f);
    printf("%f");
    printf("%.*f", f);
    printf("%.*f", f, f);
    printf("%*f", f);
    printf("%*f", f, f);
    
    printf("%#hd", si);
    printf("%hf", f);
    printf("%ho", f);
    printf("%'he", f);
    printf("%hc", i);
    
    printf("%#hhd", si);
    printf("%hhf", f);
    printf("%hho", f);
    printf("%'hhe", f);
    printf("%hhc", i);
    
    printf("%#ld", si);
    printf("%lo", f);
    printf("%lc", i);
    printf("%k", i);
    printf("%'#0c", i);
    
    printf("%#lld", si);
    printf("%llo", f);
    printf("%llc", i);
    
    printf("%#lld", si);
    printf("%llo", f);
    printf("%llc", i);
    
    printf("%#zd", si);
    printf("%zo", f);
    printf("%zc", i);
    
    printf("%#td", si);
    printf("%to", f);
    printf("%tc", i);
    
    return 0;
}