namespace NS139058 {
    typedef char *string;
}
class C139058 {
public:
    operator NS139058::string() const;
    NS139058::string asString() const { return this->operator NS139058::string(); }
};
