
#include "bug228950_Included.h"


namespace bug228950 {
    using namespace bug228950_included;
    
    int main3() {
        Field::typeOther l;
        return l.size();
        return 0;
    }
}

using namespace bug228950;

int main228950() {
    Field::typeOther l;
    return l.size() + main3();
}
