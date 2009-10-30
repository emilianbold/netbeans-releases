//#include <iostream>
int main(int argc, char**argv) {
    std::cout << "Welcome ..." << std::endl;

    // Prints arguments...
    if (argc > 1) {
        std::cout << std::endl << "Arguments:" << std::endl;
        for (int i = 1; i < argc; i++) {
            std::cout << i << ": " << argv[i] << std::endl;
        }
    }

    int i = 5;
    if (argc == 1) return argc + argv;
    if (argc == 2) return main(argc, argv);

    int a,c,d,e = 5;

    int b = 5;

    main(a,
        b,
        c);

    int x = 5;
    // comment
    int y = 5;

    if (argc == 1) { return argv; }

    return 0;
}
