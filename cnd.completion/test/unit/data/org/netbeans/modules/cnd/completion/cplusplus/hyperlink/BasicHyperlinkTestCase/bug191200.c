typedef struct fsr191200 {
    int fsr_value;
} fsr_convert_t191200;

typedef struct F191200 {
    int value;
    fsr_convert_t191200* pdata;
    fsr_convert_t191200* data;
} f_t191200;

void f191200() {
    f_t191200
    table[] = {
        { .value = 1, .data = (fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 2, .pdata = &(fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 3, .data = (fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 0}
    };    

    f_t191200
    table2[] = {
        { .value = 1, .pdata = &(fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 2, .data = (fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 3, .pdata = &(fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 0}
    };    
}
