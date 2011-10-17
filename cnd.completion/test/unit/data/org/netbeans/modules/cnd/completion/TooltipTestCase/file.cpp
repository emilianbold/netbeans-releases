int foo(int k) {
    return 2;
}

int bar() {
    return 1;
}

void zoo(int i, int j) {
}

int main() {
    zoo(foo(1), bar());
    zoo(foo(1), ((1)));
    return 0;
}