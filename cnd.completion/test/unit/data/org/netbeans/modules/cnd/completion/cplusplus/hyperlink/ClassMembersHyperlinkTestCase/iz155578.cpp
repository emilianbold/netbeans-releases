
struct IZ155578 {
    typedef float jfloat;
    typedef double jdouble;

    inline int g_isfinite(jfloat  f)                 { return (int)f; }
    inline int g_isfinite(jdouble f)                 { return (int)f; }
};
