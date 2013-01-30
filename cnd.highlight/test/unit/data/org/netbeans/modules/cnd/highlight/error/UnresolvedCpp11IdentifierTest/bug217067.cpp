template<int i>
    inline auto
    bug217067_foo(volatile int& __tuple)
    -> int volatile&
    { return 0; }