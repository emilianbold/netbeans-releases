
#include "common_tds.h"

#include <vs_internal.h>

struct format {
    int field;
    int field1;
};

struct format_t {
    int field;
    int field1;
};

void format_function(audio_format *v) {
    v->fmt->field;
    v->fmt->field1;
    v->fmt_t->field;
    v->fmt_t->field1;
}

