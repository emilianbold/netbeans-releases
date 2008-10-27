
#include "common_tds.h"

#include <vs_internal.h>

struct format {
    int field;
    int field2;
};

struct format_t {
    int field;
    int field2;
};

void format_function(audio_format *v) {
    v->fmt->field;
    v->fmt->field2;
    v->fmt_t->field;
    v->fmt_t->field2;
}

