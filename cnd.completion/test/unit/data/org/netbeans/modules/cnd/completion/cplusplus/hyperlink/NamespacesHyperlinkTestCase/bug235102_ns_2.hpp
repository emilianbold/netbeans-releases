#ifndef NEWFILE_H
#define	NEWFILE_H

namespace bug235102_ns_2 {

    namespace llvm_235102_ns_2 {
        struct AAA_235102_ns_2;
    } 

    namespace llvm_235102_ns_2 {
        struct AAA_235102_ns_2 {
            int foo();
        };
    } 

    namespace clang_235102_ns_2 {
        using llvm_235102_ns_2::AAA_235102_ns_2;
    }

    namespace clang_235102_ns_2 {
        struct BBB_235102_ns_2 {
            int zoo();
        };
    }  

}

#endif	/* NEWFILE_H */