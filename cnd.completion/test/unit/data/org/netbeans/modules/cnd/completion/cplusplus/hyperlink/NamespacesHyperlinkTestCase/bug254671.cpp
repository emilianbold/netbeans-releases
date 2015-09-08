#define DECLARE_5(name) int name##0; int name##1; int name##2; int name##3; int name##4;
#define DECLARE_25(name) DECLARE_5(name##0) DECLARE_5(name##1) DECLARE_5(name##2) DECLARE_5(name##3) DECLARE_5(name##4)
#define DECLARE_50(name) DECLARE_25(name##0) DECLARE_25(name##1)

DECLARE_50(var254671_);

struct dummy_forward254671 *var254671_51;

namespace bug254671 {
    typedef int type_254671;
}

using namespace bug254671;

void foo254671() {
    type_254671 a;
}

