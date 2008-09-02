enum { maxMediaChunks = 2 };

struct MediaSample {
    int chunk[maxMediaChunks]; // 
};

enum Style {
    feminineStyle,
    masculineStyle,
    brashStyle,
    nStyles
};

class Fad {
public:
    static const char* s_styleNames[ nStyles ];
};

const char* Fad::s_styleNames[ nStyles ] = { // 
    "feminine",
    "masculine",
    "brash"
};

typedef struct ClassOfUnnamedTypedef {
    const char *field;
};

template <int> class AEnum {};

class BVV {
    enum { VV = 1 };
    AEnum<VV> a;
};

void fooWW() {
    enum { WW = 2 };
    AEnum<WW> b;
    ClassOfUnnamedTypedef obj;
    obj.field;
}
