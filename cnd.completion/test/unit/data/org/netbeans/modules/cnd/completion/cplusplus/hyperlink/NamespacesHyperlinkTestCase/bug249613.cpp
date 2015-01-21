namespace bug249613 {
    namespace A249613 {
        inline namespace __1 {
            namespace B249613 {
                void foo249613() {
                }
            }
            
            struct InlinedStruct249613 {};
        }

        namespace {
            namespace C249613 {
                void boo249613() {
                }
            }
            struct UnnamedStruct249613 {};
        }
    }

    int main249613() {
        A249613::B249613::foo249613();
        A249613::C249613::boo249613();
        A249613::InlinedStruct249613 st1;
        A249613::UnnamedStruct249613 st2;
        return 0;
    }
}