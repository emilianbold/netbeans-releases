namespace bug240016 {
    struct my_struct240016 {
      const char *identifier240016;
      int xx;
      double zz;
    };

    const char * identifier240016 = "test";

    static my_struct240016 my_struct_instance240016 = { 
        identifier240016 : identifier240016,
        .xx = 1,
        zz : 1.0,
    };
    
    struct teststruct240016 {
        int xx;
        int yy;
    };    
    
    struct testouterstruct240016 {
        int yy = 1;      
        
        struct teststruct240016 inner = {
            xx : 1,
            yy : yy
        };
        
    };    
}