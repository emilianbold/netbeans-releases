namespace {

    namespace inner_235102cc {
    
        struct AAA_235102cc {
            int foo();
        };
        
        struct cast_235102cc_retty {};

        AAA_235102cc cast_235102cc(int);
    }
    
    int boo() {
        inner_235102cc::cast_235102cc(1).foo();
    } 
}
