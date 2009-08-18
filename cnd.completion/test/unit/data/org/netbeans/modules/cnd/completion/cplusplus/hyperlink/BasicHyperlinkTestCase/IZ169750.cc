struct IZ169750_dirent {
  int qwe;
};
int IZ169750_main(int argc, char** argv) {
    while (struct IZ169750_dirent *de = 0) { // de is unresolved
    };
    struct IZ169750_dirent *de2; // ok
    de2->qwe = 0; // ok
}