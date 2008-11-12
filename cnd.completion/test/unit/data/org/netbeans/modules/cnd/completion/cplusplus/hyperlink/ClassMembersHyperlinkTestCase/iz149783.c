struct iz149783_S {
    int field;
    iz149783_S ar[];
    iz149783_S ar2[5];
};

void iz149783_foo() {
    iz149783_S ss[1];
    ss->field;
    ss->ar->field;
    ss->ar2->field;
    iz149783_S ss2;
    ss2.ar2[0].ar->field;
}

