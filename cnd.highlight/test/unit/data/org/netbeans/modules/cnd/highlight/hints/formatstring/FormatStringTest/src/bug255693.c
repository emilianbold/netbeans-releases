#include <stdio.h>

int main(int argc, char** argv) {
    char* format_str = "%s%lld";
    printf(format_str, "test", (long long) 12);
    return 0;
}