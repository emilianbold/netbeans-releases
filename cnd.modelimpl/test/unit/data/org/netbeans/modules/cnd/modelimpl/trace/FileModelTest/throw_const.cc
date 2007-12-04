
void* mynew_a(unsigned int sz) throw (const char) {
    void* p = malloc(sz);
    return p;
}


void* mynew_b(unsigned int sz) throw (const char&) {
    void* p = malloc(sz);
    return p;
}
