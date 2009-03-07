namespace boost {
    namespace regex_constants {
        typedef enum _match_flags {
            match_default = 0,
        } match_flags;
        typedef match_flags match_flag_type;
    }
    using regex_constants::match_flag_type;
    using regex_constants::match_default;
}

namespace boost {
    namespace re_detail {
        match_flag_type m_match_flags;
        void foo() {
            if (m_match_flags & match_default) {} // match_default is not resolved
        }
    }
}
int main() {
    return 0;
}
