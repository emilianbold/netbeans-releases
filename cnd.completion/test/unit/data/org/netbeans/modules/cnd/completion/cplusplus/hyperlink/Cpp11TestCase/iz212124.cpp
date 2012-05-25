namespace classMembersEnums212124
{
    struct S212124
    {
        enum { A212124 = 1, B212124 = 2 };
        struct T212124
        {
            enum { B212124 = 102 };

            enum class E1_212124;
            enum E2_212124 : int;
        };
    };

    enum class S212124::T212124::E1_212124 { A1_212124 = A212124, B1_212124 = B212124, C1_212124 };
    enum S212124::T212124::E2_212124 : int { A1_212124 = A212124, B1_212124 = B212124, C1_212124 };

    static_assert(int(S212124::T212124::E1_212124::A1_212124) == 1, "error");
    static_assert(int(S212124::T212124::E1_212124::B1_212124) == 102, "error");
    static_assert(int(S212124::T212124::E1_212124::C1_212124) == 103, "error");

    static_assert(int(S212124::T212124::E2_212124::A1_212124) == 1, "error");
    static_assert(int(S212124::T212124::E2_212124::B1_212124) == 102, "error");
    static_assert(int(S212124::T212124::E2_212124::C1_212124) == 103, "error");
    static_assert(int(S212124::T212124::A1_212124) == 1, "error");
    static_assert(int(S212124::T212124::B1_212124) == 102, "error");
    static_assert(int(S212124::T212124::C1_212124) == 103, "error");
}
