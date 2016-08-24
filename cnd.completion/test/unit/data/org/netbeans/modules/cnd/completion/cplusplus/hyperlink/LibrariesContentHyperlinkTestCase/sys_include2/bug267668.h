#ifndef BUG267668 
#define BUG267668

namespace ns1_267668 {
    struct StrRef267668 {
        void foo();
    };
}

namespace ns2_267668 {
    using ns1_267668::StrRef267668;
}

#endif