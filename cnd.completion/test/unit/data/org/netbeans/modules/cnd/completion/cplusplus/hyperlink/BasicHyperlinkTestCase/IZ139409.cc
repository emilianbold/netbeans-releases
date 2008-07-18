int start = 5;
int main() {
    int sum = start;
    start:
    if (sum < 10)
        goto start;
    int end = 0;
    goto end;
    return sum;
}
