
#include "IZ141601_static_fun_in_hdr.h"
void use_static_inline_add() {
    static_inline_add(1, 2); // highlighted as unresolved
}
