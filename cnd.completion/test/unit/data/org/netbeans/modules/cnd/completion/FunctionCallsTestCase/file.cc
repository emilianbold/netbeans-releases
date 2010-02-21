int a(int x) {
    return x;
}

int b(int x) {
    return x;
}

double a(double x) {
    return x;
}

double b(double x) {
    return x;
}

int main(int argc, char**argv) {
    int q = a(b(1));
    double c = a(b(1.0));
    int y = a(1) + b(2) + q;
    // Prints welcome message...
    std::cout << "Welcome ..." << std::endl;

    // Prints arguments...
    if (argc > 1) {
        std::cout << std::endl << "Arguments:" << std::endl;
        for (int i = 1; i < argc; i++) {
            std::cout << i << ": " << argv[i] << std::endl;
        }
    }

    return 0;
}
