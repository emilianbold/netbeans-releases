int bug217858_main(int argc, char** argv) {
    static_assert(true, u8"aa");
    return 0;
}
