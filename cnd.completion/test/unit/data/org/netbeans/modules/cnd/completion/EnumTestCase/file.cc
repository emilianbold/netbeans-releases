
class ClassA {
public:
    enum EN { ONE,  TWO };
    void aPubFun();
};

void ClassA::aPubFun() {
     // <- test text is inserted here
     
}

enum EN1 { ONE1,  TWO1 };

void foo() {
     // <- test text is inserted here
    ClassA::EN en;
    en = ClassA::ONE;
    ClassA aa;
     // <- test text is inserted here
}
