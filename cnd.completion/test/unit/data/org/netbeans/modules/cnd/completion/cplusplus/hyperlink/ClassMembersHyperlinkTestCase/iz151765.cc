class iz151765_C {
public:
    int i;
};

int main() {
    iz151765_C m[2];
    iz151765_C *p = m;

    p->i++; // ok
    (p+1)->i++; // unresolved

    return 0;
}