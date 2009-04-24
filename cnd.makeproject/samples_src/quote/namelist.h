/*
 * Copyright (c) 2009, Sun Microsystems, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
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
