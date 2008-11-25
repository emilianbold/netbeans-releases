namespace IZ153761 {

#define FOR_MACRO for (;;) {
#define WHILE_MACRO while (1) {
#define IF_MACRO if (1) {
#define IF_ELSE_MACRO if (1) {} else {
#define CLASS_MACRO class MyClass {
#define CLASS_MACRO_2(x) class MyClass2 : x {
#define SWITCH_MACRO switch (0) {
#define TRY_MACRO try {
#define TRY_CATCH_MACRO try {} catch (MyClass* p) {
#define LCURLY_INT_MACRO { int

    void foo() {
        FOR_MACRO
            int a;
        }
        WHILE_MACRO
            int b;
        }
        IF_MACRO
            int c;
        }
        IF_ELSE_MACRO
            int d;
        }
        CLASS_MACRO
            int e;
        };
        CLASS_MACRO_2(MyClass)
            int MyClass;
        };
        SWITCH_MACRO
        default:
            int f;
        }
        TRY_MACRO
            int g;
        }
        TRY_CATCH_MACRO
            int h;
        }
        LCURLY_INT_MACRO
            i;
        }
    }

}
