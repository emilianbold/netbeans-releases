struct AAA269201 {
    struct {
        int s_first;
        float s_second;
    };
    union {
        int u_first;
        float u_second;
    };
    struct {
        int xxx;
    } fld;
    int boo;
}; 

void bla269201() {
    struct AAA269201 var;
    var.s_first = 1;
    var.s_second = 1.0f;
    var.u_first = 1;
    var.u_second = 1.0f;
    var.fld.xxx = 1;
}