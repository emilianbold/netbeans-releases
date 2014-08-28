#include <bug244777.h>

namespace bug244777 {    
    class TestStruct244777 {
    private:
        int foo();

        friend struct std244777::hash244777<TestStruct244777>;
    };
    
    namespace std244777 {
        template <>
        struct hash244777<TestStruct244777> {
            int mtd(TestStruct244777 &param) {
                return param.foo();
            }
        };
    }
}