class NewClass {
public:
    static int AAA;
    static int BBB;
    static int CCC;
private:

};

int NewClass::AAA = 10;
int NewClass::BBB(AAA);
int NewClass::CCC = BBB;
