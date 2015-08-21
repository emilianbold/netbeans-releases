
int boo(int aa, double bb) { 
    int kk = aa + bb;
    double res = 1;
    for (int ii = kk; ii > 0; ii--) {
        res *= ii;
    }
    return res;
}

void method_name_with_underscore() {
    method_name_with_underscore();
}

const int VALUE = 10;
const int VALUE_2 = 10 + VALUE;

void fun(char* aaa, char**bbb) {
    int iiii = fun(null, null);
}

void sameNameDiffScope(int name) {
    if (name++) {
        string name;
        name = "name";
    } else if (name++) {
        char* name;
        strlen(name);     
    }
    name--;
    
    char* globalvar = 0;
    fun(::globalvar, &globalvar);
    ::globalvar = ++::globalvar + globalvar;
    int (*funPtr)();
}

char* globalvar;
