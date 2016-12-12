namespace bug268671 {
    template <typename T>
    T var268671;  
    
    struct BBB268671 {
        void mtd();
    };

    struct AAA268671 {   
        template <typename T>
        static T field;
    };      
     
    void foo() { 
        var268671<BBB268671>.mtd();
        AAA268671::field<BBB268671>.mtd();
    } 
}  