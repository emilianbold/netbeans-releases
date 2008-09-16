
namespace mystd {
    class string {
    };
}

namespace litesql {
    using namespace mystd;
    class UpdateQuery {
        string table;
    public:
        operator string() const;
    };
}

namespace litesql {
    using namespace mystd;

    UpdateQuery::operator mystd::string() const {
        string q = table;
        return q;
    }
}

namespace litesql2 {
    using namespace mystd;
    class UpdateQuery {
        string table;
    public:
        operator mystd::string() const;
    };
}

namespace litesql2 {
    using namespace mystd;

    UpdateQuery::operator string() const {
        string q = table;
        return q;
    }
}