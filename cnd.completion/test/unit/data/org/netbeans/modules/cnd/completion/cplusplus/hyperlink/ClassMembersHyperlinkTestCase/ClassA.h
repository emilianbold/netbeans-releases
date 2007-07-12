typedef int myInt;
class ClassA {
public:
    virtual ~ClassA(); // in test testDestructors
    
public:
    ClassA(); // in test testConstructors

    void publicFoo(); // in test testPublicMethods
    void publicFoo(int a); // in test testPublicMethods
    void publicFoo(int a, double b); // in test testPublicMethods
    void publicFoo(ClassA a); // !!!FAILED!!!
    void publicFoo(const ClassA &a); // !!!FAILED!!!
    
    static void publicFooSt(); // in test testPublicMethods
    
protected:
    ClassA(int a); // in test testConstructors
    
    void protectedFoo(); // in test testProtectedMethods
    void protectedFoo(int a); // in test testProtectedMethods
    void protectedFoo(int a, double b); // in test testProtectedMethods
    void protectedFoo(const ClassA* const ar[]);    // !!!FAILED!!!
    
    static void protectedFooSt(); // in test testProtectedMethods
private:
    ClassA(int a, double b); // in test testConstructors
    void privateFoo(); // in test testPrivateMethods
    void privateFoo(int a); // in test testPrivateMethods
    void privateFoo(int a, double b); // in test testPrivateMethods
    void privateFoo(const ClassA *a); // in test testPrivateMethods
    
    static void privateFooSt(); // in test testPrivateMethods
// members
public:
    int publicMemberInt;
    double publicMemberDbl;
    static int publicMemberStInt;
    
protected:
    int protectedMemberInt;
    double protectedMemberDbl;
    static int protectedMemberStInt;
    
private:
    int privateMemberInt;
    double privateMemberDbl;
    static int privateMemberStInt;
    
//operators
public:
    ClassA& operator= (const ClassA& obj); // in test testOperators
protected:
    ClassA& operator+ (const ClassA& obj); // in test testOperators
private:
    ClassA& operator- (const ClassA& obj); // in test testOperators
    
private:
    ClassA* classMethodRetClassAPtr();
    const ClassA& classMethodRetClassARef();
    
    typedef int myInnerInt;

    myInt classMethodRetMyInt();
    
    myInnerInt classMethodRetMyInnerInt();
    
private:
    friend ostream& operator<< (ostream&, const ClassA&);

public:
    friend void friendFoo();
};


