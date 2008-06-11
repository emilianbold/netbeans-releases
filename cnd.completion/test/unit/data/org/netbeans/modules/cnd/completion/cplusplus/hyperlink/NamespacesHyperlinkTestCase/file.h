
#ifndef _FILE_H_
#define _FILE_H_

namespace S1 {
    extern int var1;
    void foo();
    namespace S2 {
        extern int var2;
        void boo();

        class clsS2 {
        public:
            void clsS2pubFun();
        };
    }
    
    class clsS1 {
    public:
        void clsS1pubFun();
    };

    template <T> class myBasic {
        // Types:
    public:
        typedef T& reference;
    };
    
    typedef myBasic<char>    myType;    
}

#endif
