/* 
 * File:   StdHeader228949.h
 * Author: vvoskres
 *
 * Created on August 21, 2013, 1:57 PM
 */

#ifndef STDHEADER228949_H
#define	STDHEADER228949_H

namespace Std228949 {
    struct string {
        int length() { return 0; }
    };
    
    template<class T> class vector {
    private:
        T elems[];
    public:
        int size() {
            return 0;
        }
    };
}

using namespace Std228949;

#endif	/* STDHEADER228949_H */

