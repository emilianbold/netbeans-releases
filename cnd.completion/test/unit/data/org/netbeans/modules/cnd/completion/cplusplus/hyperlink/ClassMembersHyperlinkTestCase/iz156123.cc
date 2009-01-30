namespace litesql156123 {
    class Database {

    };
}

using namespace litesql156123;

namespace xml156123 {
    class Database {
    public:
        class Field {
        public:
            int name;
        };
    };
}

namespace xml156123 {
    static void foo() {
        Database::Field fld;// Field is unresolved
        fld.name = 1; // OK
    }
}