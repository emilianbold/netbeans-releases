#ifndef _OPERATORS_H_
#define _OPERATORS_H_

class Cls {
    public:
      Cls& operator =  (const Cls&);
      Cls& operator += (const Cls&);
      Cls& operator -= (const Cls&);
      Cls& operator *= (const Cls&);
      Cls& operator /= (const Cls&);
};

//void operator << (ClassWithOps& obj, int);

#endif // _OPERATORS_H_
