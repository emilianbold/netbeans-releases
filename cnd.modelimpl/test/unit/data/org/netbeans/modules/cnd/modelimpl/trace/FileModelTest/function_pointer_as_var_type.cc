int plus(int x, int y)  { return x + y; }
int minus(int x, int y) { return x - y; }

int (*fp_plus)(int, int) = plus;
int (*fp_minus)(int, int) = minus;
