/* 
 * File:   Project228949.h
 * Author: vvoskres
 *
 * Created on August 21, 2013, 1:57 PM
 */

#ifndef PROJECT228949_H
#define	PROJECT228949_H
 
namespace NNOther228949 {
    struct BBB228949 {
        string& str() { return 0; }
    };
}

namespace NN228949 {
    class AAA228949 : public NNOther228949::BBB228949 {
    private:
        vector<string> field;
    public:
        AAA228949() : BBB228949() {
            
        }
        vector<string>& get() {
            return field;
        }

        int size() {
            return field.size() + str.length();
        }   
        
        string& getStr() { 
            BBB228949 base;
            return base.str();
        }
        
        string& getSSS();

    private:
        string str;
    };
}

#endif	/* PROJECT228949_H */
