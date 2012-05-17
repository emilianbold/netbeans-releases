class bug211033_ClassOuter {
public:
    bug211033_ClassOuter();
    bug211033_ClassOuter(const bug211033_ClassOuter& orig);
    virtual ~bug211033_ClassOuter();

    class bug211033_Class {
    public:
        bug211033_Class();
        bug211033_Class(const bug211033_Class& orig);
        virtual ~bug211033_Class();
    private:
        class bug211033_StringRef;
        bug211033_StringRef* pNext;
    };
private:

};

class bug211033_Other {
public:
    bug211033_Other();
    bug211033_Other(const bug211033_Other& orig);
    virtual ~bug211033_Other();
private:
    class bug211033_StringRef* pNext;
};

class bug211033_StringRef {
public:
    void foo();
};

class bug211033_ClassOuter::bug211033_Class::bug211033_StringRef {
public:
    void boo();
};

bug211033_Other::bug211033_Other() {
    pNext = 0;
    pNext->foo();
}

bug211033_ClassOuter::bug211033_Class::bug211033_Class() {
    pNext = 0;
    pNext->boo(); // unresolved boo
}