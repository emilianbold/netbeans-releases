namespace bug235102_ns {
    namespace A235102_ns {
        struct AAA235102_ns {
            int foo();
        };    
    }

    namespace B235102_ns {
        using namespace A235102_ns;
    }

    namespace B235102_ns {
        namespace C235102_ns {
            AAA235102_ns x;

            int function235102_ns() {
                x.foo(); // foo is unresolved
            }
        } 
    }
    
    namespace UD_A_235102 {
        struct Test235102 {
            int foo();
        };
    }
    
    namespace UD_B_235102 {
        using UD_A_235102::Test235102;
    }
    
    namespace UD_B_235102 {
        namespace UD_C_235102 {
            Test235102 x;

            int ud_func_235102() {
                x.foo(); // foo is unresolved
            }
        }         
    }
}