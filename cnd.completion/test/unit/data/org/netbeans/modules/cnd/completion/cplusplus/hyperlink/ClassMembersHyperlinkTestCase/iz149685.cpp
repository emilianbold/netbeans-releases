class MxfTypes {
public:
    class ULPair {
    private:
        struct Name {
            const char* alias;
            Name(const Name& rhs) {
                alias = rhs.alias; // alias unresolved
            }
        };
    public:
        void checkName();
    private:
        Name m_name;
    };
};

void MxfTypes::ULPair::checkName() {
    m_name.alias;
}