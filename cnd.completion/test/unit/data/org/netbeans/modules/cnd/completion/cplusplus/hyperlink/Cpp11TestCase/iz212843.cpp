namespace iz212843 {
    enum { AA = 1, BB = 2 };
    struct TT
    {
        enum { BB = 102, DD = 103, BA = AA};
        class E1;
        enum class E2 : int;
    };

    class TT::E1 {
        static int f = BB;
    };

    enum class TT::E2 : int { A1 = AA, B1 = BB, C1 = DD };
}
