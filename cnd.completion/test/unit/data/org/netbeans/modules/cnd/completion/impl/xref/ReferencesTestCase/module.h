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

#if !defined MODULE_H
#define MODULE_H

//#include <iostream>

using namespace std;

// Base class

class Module {
public:
    Module();
    Module(const char* description, const char* vendor, int type, int category, int units);
    virtual ~Module(); //destructor is virtual since derived classes may have distinct destructor

    Module(const Module& obj); //copy constructor
    Module& operator= (const Module& obj); //overload of assignment operator "="

    void SetDescription(const char* description);
    const char* GetDescription() const;

    void SetVendor(const char* v);
    const char* GetVendor() const;

    void SetType(int type);
    int GetTypeID() const;
    virtual const char* GetType() const = 0;

    void SetCategory(int category);
    int GetCategoryID() const;
    virtual const char* GetCategory() const = 0;

    void SetUnits(int u);
    int GetUnits() const;

    void SetSupportMetric(int m);
    int GetSupportMetric() const;

protected:    
    virtual void ComputeSupportMetric() = 0; //metric is defined in derived classes

 private:
    string  description;
    string  vendor; //this anticipates future functionality
    int     type;
    int     category;
    int     units;
    int     supportMetric; //default value

friend ostream& operator<< (ostream&, const Module&);
};

#endif // MODULE_H
