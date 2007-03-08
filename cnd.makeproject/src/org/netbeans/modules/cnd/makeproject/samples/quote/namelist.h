/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

#if !defined NAME_LIST_H
#define NAME_LIST_H

enum tDiscount {tnone, tretail, trepeat};

class NameList // ?database? of names. Singleton class.
{
    private:
        static NameList* pList; //used to implement singleton design pattern
        
        int nameCount;
        int maxIndex;
        
        char** Name; //2D character array ... name strings ... dynamically allocated in constructor
        char** ID; //2D character array ... ID strings ... dynamically allocated in constructor
        int* Index;
        int* Discount;
        
        protected:
            NameList(); //constructor ... called indirectly through singleton pattern (static ListInstance)
            NameList(const NameList& obj); //copy constructor ... not called directly
            NameList& operator=(NameList& obj); //overload of assignment operator ... not called directly
            ~NameList(); //destructor ... not called directly
            public:
                static NameList* ListInstance(); //used to implement singleton design pattern (check plist)
                int FindCustomer(char* name); //returns customer index within namelist data structure
                char* GetName(int index);
                char* GetID(int index);
                int GetDiscount(int index);
                void DisplayList();
}
;//note that ";" is required at end of class definition header file

#endif //NAME_LIST_H
