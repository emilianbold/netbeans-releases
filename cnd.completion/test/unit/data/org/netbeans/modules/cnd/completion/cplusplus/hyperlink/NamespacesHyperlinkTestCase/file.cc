
#include "file.h"

namespace S1 {
    int var1;

    void foo() {
        // S1 content must be visible with and without S1:: prefix
        S1::foo();
        S1::var1 = 10;
        foo();
        var1 = 11;
        // S2 content must be visible with prefixes
        S1::S2::boo();
        S1::S2::var2 = 100;
        S2::boo();
        S2::var2 = 101;
    }

    namespace S2 {
        int var2;
        
        void boo() {
            // S1 content must be visible with and without S1:: prefix
            S1::foo();
            S1::var1 = 12;
            foo();
            var1 = 13;
            // S2 content must be visible with and without prefixes
            S1::S2::boo();
            S1::S2::var2 = 102;
            S2::boo();
            S2::var2 = 103;
            boo();
            var2 = 104;
        }
        
        void funS2() {
            clsS1 s1;
            s1.clsS1pubFun();            
            
            clsS2 s2;
            s2.clsS2pubFun();
        }
        
        void clsS2::clsS2pubFun() {
            
        }
    }
    
    void funS1() {
        clsS1 s1;
        s1.clsS1pubFun();
        
        S2::clsS2 s2;
        s2.clsS2pubFun();
    }
    
    void clsS1::clsS1pubFun() {
        
    }
    
    extern int myCout;
}

