
void bug217827_foo() {
    int blah[1];

    for (const auto s : blah)
        s++;
    
}
