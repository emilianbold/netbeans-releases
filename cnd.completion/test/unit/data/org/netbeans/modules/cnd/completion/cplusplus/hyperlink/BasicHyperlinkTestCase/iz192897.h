

namespace iz192897 {

    class Cursor {
            void foo();
        public:
            void dump();
            int field;
    };

    class Wrapper {
        class Cursor {
            void foo();
            public:
                int field2;
        };


    };
      

    void Cursor::foo() {
        int var = field;
    }

}


