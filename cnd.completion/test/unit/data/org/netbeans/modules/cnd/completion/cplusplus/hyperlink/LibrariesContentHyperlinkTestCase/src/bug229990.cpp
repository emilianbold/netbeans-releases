#include <sys_stat_h.h>
#include "bug229990.h"

namespace AAAA229990 {
    namespace Inner229990 {
        void A229990::foo229990(struct stat229990* stat, struct ssss229990* sss) {
                        // stat it
                        struct stat229990 sb1;
                        // get length from stat
                        long _length = sb1.st_size;

                        struct ssss229990 sb2;
                        _length = sb2.size;
        }
    }
}
