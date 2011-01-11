namespace OuterNSIncluder {
    namespace InnnerNSIncluder {
#include "innernsbody.h"
        struct OuterClassIncluder {
#define PART1
#include "outerbody.h"
#undef PART1
            struct InnerClassIncluder {
#include "innerbody.h"

                void booIncluder() {
#include "methodbody.h"
                    localvarIncluder = 1;
                }
            };
#define PART2
#include "outerbody.h"
#undef PART2
        };
    }
#include "outernsbody.h"
}
 