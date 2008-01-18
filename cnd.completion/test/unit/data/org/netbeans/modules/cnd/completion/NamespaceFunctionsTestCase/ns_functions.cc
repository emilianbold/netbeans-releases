namespace org {
    int o_var;
    namespace netbeans {
        void o_n_foo_1();
        void o_n_foo_2();
        int o_n_var;
        class ONClass {
            int f1;
            void meth_1();
            void meth_2();
        };
    }
}

namespace org {
    namespace netbeans {
        void o_n_foo_1() {
              // CC here
        }
    }
}

void org::netbeans::o_n_foo_2() {
     // CC here
}

void org::netbeans::ONClass::meth_1() {
     // CC here
}