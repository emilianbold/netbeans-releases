
#ifndef _FILE_H_
#define _FILE_H_

class ClassC;

class ClassA {
public:
    int aPub;
    int static aPubSt;
    void aPubFun();
    void static aPubFunSt();
protected:
    int aProt;
    int static aProtSt;
    void aProtFun() {}
    void static aProtFunSt();
private:
    int aPriv;
    int static aPrivSt;
    void aPrivFun() {}
    void static aPrivFunSt();
};
 
class ClassB : private ClassA {
public:
    int bPub;  
    int static bPubSt;
    void bPubFun() {}
    void static bPubFunSt();
protected:
    int bProt;
    int static bProtSt;
    void bProtFun();
    void static bProtFunSt();
private:
    int bPriv;
    int static bPrivSt;
    void bPrivFun() {}
    void static bPrivFunSt();
};

class ClassC {
public:
    int cPub;   
    int static cPubSt;
    void cPubFun() {}
    void static cPubFunSt();
protected:
    int cProt;
    int static cProtSt;
    void cProtFun() {}
    void static cProtFunSt();
private:
    int cPriv;
    int static cPrivSt;
    void cPrivFun();
    void static cPrivFunSt();
};
 
class ClassD : public ClassB, protected ClassC {
public:
    int dPub;
    int static dPubSt;
    void dPubFun();
    void static dPubFunSt();
protected:
    int dProt;
    int static dProtSt;
    void dProtFun() {}
    void static dProtFunSt();
private:
    int dPriv;
    int static dPrivSt;
    void dPrivFun() {}
    void static dPrivFunSt();
};

class ClassE : protected ClassC {
public:
    int ePub;
    int static ePubSt;
    void ePubFun();
    void static ePubFunSt();
protected:
    int eProt;
    int static eProtSt;
    void eProtFun() {}
    void static eProtFunSt();
private:
    int ePriv;
    int static ePrivSt;
    void ePrivFun() {}
    void static ePrivFunSt();
};

#endif
