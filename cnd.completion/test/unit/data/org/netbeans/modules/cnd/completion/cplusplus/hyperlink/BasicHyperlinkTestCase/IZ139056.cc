namespace my1 {
    struct type1 {};
}

namespace my2 {
    using namespace my1;
}

namespace my2 {
    type1 transform(type1);
}

namespace my3 {
    using namespace my2;
    type1 transform(type1);
}
