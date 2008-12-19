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

#include "namelist.h"
#include <cstdlib>
#include <iostream>
#include <cstring>
using namespace std;


NameList* NameList::ListInstance() {
    if (pList) //Check to see if NameList already exists (plist not null)
    {
        return pList;
    }
    else {
        pList=new NameList; //Create NameList. Creator function will be called.
        return pList;
    }
}

NameList::NameList() {
    maxIndex=4;
    const int sz=80;
    
    Name=new char* [maxIndex];
    ID=new char* [maxIndex];
    
    for(int i=0;i<=maxIndex;i++) {
        *(Name+i)=new char[sz];
        *(ID+i)=new char[sz];
    }
    
    Index=new int[maxIndex];
    Discount=new int[maxIndex];
    
    for (int i=0; i<=maxIndex; i++) {
        switch(i) {
            case 0: Name[0]=(char*)"XYZ";ID[0]=(char*)"111";Index[0]=0;Discount[0]=trepeat;break;
            case 1: Name[1]=(char*)"RSG";ID[1]=(char*)"112";Index[1]=1;Discount[1]=trepeat;break;
            case 2: Name[2]=(char*)"AEC";ID[2]=(char*)"113";Index[2]=2;Discount[2]=trepeat;break;
            case 3: Name[3]=(char*)"John";ID[3]=(char*)"0";Index[3]=3;Discount[3]=tretail;break;
            case 4: Name[4]=(char*)"Mary";ID[4]=(char*)"0";Index[4]=4;Discount[4]=tretail;break;
            default:	;
        }
    }
    
}

NameList::NameList(const NameList& obj) //copy constructor is not supported for this class
{
}

NameList& NameList::operator=(NameList& obj) //overload of assignment not supported for this class
{
    return *this;
}

NameList::~NameList() {
    delete [] Name;
    delete [] ID;
    delete [] Index;
    delete [] Discount;
    
}

int NameList::FindCustomer(char* name) {
    for (int i=0; i<=maxIndex; i++) {
        if (strcmp(Name[i], name)==0)
            return Index[i];
    }
    return -1;//not found
}

char* NameList::GetName(int index) {
    return Name[index];
}

char* NameList::GetID(int index) {
    return ID[index];
}

int NameList::GetDiscount(int index) {
    return Discount[index];
}

void NameList::DisplayList() {
    cout<<"**Namelist content**"<<endl;
    for (int i=0; i<=maxIndex; i++)
        cout<<"Name: "<<Name[i]<<" Discount code: "<<Discount[i]<< " ID: "<<ID[i]<<endl;
    cout<<endl;
}

//end namelist.cc
