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
}