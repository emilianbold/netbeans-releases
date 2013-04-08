namespace ns97120{
    class Class97120;
    typedef Class97120* PClass97120;
    typedef PClass97120 PPClass97120;
    typedef PPClass97120 PPPClass97120;
    
    class Class97120 {
    public:
        Class97120(int i);
        int foo();
        Class97120* ptr();
        PClass97120 pPtr();
        PPClass97120 ppPtr();
        PPPClass97120 pppPtr();
    private:

    };


    
    /*
     * 
     */
    int main97120(int argc, char** argv) {
            Class97120 c(10), d(1);
            PClass97120 pA = new Class97120(10);
            PPClass97120 ppA = new Class97120(1);
            PPPClass97120 pppA = new Class97120(3);
            
            /* insert text above this line */
            
            
    }
}
