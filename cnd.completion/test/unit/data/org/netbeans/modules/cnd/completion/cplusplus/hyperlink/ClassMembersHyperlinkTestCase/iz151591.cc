class iz151591_Parent {
protected:
    void pfoo() {
    }
};

class iz151591_Child : public iz151591_Parent {
public:
    class Inner {
    public:
        void ifoo() {
            iz151591_Child m;
            m.pfoo();
        }
    };
};

int iz151591_main() {
    iz151591_Child::Inner in;
    in.ifoo();
    return 0;
}