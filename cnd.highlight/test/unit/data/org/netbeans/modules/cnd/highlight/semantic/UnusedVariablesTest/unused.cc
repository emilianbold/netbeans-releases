class UnusedClass {
private:
    int unused_variable;
};

int main(int argc, char** argv) {
    int foo;
    char bar(0), bax;
    if (1) {
        char* baz[] = 0;
    }
    return argc + foo;
}

void func(void *funcparam);
