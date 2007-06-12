
#ifndef _FILE_H_
#define _FILE_H_

class ClassC;

class ClassA {
public:
    int aPub;
    void aPubFun();
protected:
    int aProt;
    void aProtFun() {}
private:
    int aPriv;
    void aPrivFun() {}
};
 
class ClassB : private ClassA {
public:
    int bPub;  
    void bPubFun() {}
protected:
    int bProt;
    void bProtFun();
private:
    int bPriv;
    void bPrivFun() {}
};

class ClassC {
public:
    int cPub;   
    void cPubFun() {}
protected:
    int cProt;
    void cProtFun() {}
private:
    int cPriv;
    void cPrivFun();
};
 
class ClassD : public ClassB, protected ClassC {
public:
    int dPub;
    void dPubFun();
protected:
    int dProt;
    void dProtFun() {}
private:
    int dPriv;
    void dPrivFun() {}
};

class ClassE : protected ClassC {
public:
    int ePub;
    void ePubFun();
protected:
    int eProt;
    void eProtFun() {}
private:
    int ePriv;
    void ePrivFun() {}
};

#endif

