template <class T> class bug151199_C {
};

int bug151199_main() {
    bug151199_C<int (int p)> c; // unresolved p
    return 0;
}