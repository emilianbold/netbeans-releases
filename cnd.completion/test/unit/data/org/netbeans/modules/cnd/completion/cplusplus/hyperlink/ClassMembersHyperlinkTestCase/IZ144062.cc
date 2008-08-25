static void type_test()
{
    struct Uni {
        int ii;
        unsigned bit1:1;
        unsigned bit2:2;
        union {
            int j;
            char c;
        } u;
        void foo(int x) {
            int y = x + 1;
        }
    } uni;
    struct Uni2{}uni2;
}
