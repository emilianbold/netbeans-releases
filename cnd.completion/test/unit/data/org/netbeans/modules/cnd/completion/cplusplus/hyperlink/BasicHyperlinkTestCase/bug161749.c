void bug161749_f1(void *restrict x); // OK
void bug161749_f1(void *restrict x) {} // OK

void bug161749_f2(void *restrict const x);
void bug161749_f2(void *restrict const x) {}

void bug161749_f3(void *__restrict const x);
void bug161749_f3(void *__restrict const x) {}

void bug161749_f4(void *__restrict__ const x);
void bug161749_f4(void *__restrict__ const x) {}