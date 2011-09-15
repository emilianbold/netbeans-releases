#define bug201237__T(x) x
#define bug201237_NULL 0

struct bug201237_A {
    char* text() {
        return "";
    }
    
    char* foo(const char*) {
        return "";
    }
    
};    
    
int bug201237_main(int argc, char** argv) {
    bug201237_A* lowerTerm;
    bug201237_A lowerTerm2;
    lowerTerm != bug201237_NULL ? lowerTerm->text() : bug201237__T("NULL");
    lowerTerm2.foo(lowerTerm != bug201237_NULL ? lowerTerm->text() : bug201237__T("NULL"));
    
    return 0;
}