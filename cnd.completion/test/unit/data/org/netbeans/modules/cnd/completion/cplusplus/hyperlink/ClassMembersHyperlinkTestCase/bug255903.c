enum path255903 {BASE255903, INCLUDE255903};
typedef struct _ExtDescription255903 {
    enum path255903 ebase255903;
} PathDescription255903;
struct _Lib255903 {
    struct flags_t255903 {
        _Bool no_static255903;
    } flags255903;
    PathDescription255903 *path_desc255903;
};
typedef struct _Lib255903 lib_t255903;
static lib_t255903 LibraryTable255903[] = {{
        .path_desc255903 = (PathDescription255903[]){
            { .ebase255903 = INCLUDE255903 },
            { .ebase255903 = BASE255903 },},
        .flags255903 = { .no_static255903 = 1},
    }};

int main255903(int argc, char** argv) {
    LibraryTable255903[0].path_desc255903[0];
    return 0;  
}