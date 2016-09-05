#include <bug267668.h>

namespace ns2_267668 {
    namespace ns3 {
        static void foo267668() {
            StrRef267668 ref;
            ref.foo(); 
        } 
    }
}  