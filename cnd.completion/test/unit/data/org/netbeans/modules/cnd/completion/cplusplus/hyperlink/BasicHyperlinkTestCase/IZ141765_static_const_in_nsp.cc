#include "IZ141765_static_const_in_nsp.h"

namespace boost {
    int using_boost_regex_constants(int x) {
            switch(x) {
                case 0:
                    return regex_constants::syntax_char;
                case 1:
                    return regex_constants::syntax_open_mark;
            }
            return 0;
    }
}
