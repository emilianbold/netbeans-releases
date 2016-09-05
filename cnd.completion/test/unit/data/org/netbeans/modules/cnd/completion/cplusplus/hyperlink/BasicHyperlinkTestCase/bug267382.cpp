namespace bug267382 {
    class Base267382 {
    private:
        class Inner267382 {
        private:
            static int y267382;
            virtual void foo267382();
        };
    };
    
    class Middle267382 : public Base267382 {};

    class Derived267382 : public Middle267382 {};

    int Derived267382::Inner267382::y267382 = 0;
    
    void Derived267382::Inner267382::foo267382() { 
        y267382 = 10;
        return;
    }   
}