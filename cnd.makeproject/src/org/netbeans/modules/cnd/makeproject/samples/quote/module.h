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

// Base class

class Module
{
private:
	char* description;
	char* vendor; //this anticipates future functionality
	int type;
	int category;
	int units;
	int supportMetric; //default value

public:
	Module();
	virtual ~ Module(); //destructor is virtual since derived classes may have distinct destructor

	Module(const Module& obj); //copy constructor
	 
	Module& operator=(Module& obj); //overload of assignment operator "="

	int SetDescription(char* d); //returns errorcode
	char* GetDescription();

	int SetVendor(char* v); // returns errorcode
	char* GetVendor();

	void SetType(int t);
	int GetType();

	void SetCategory(int c);
	int GetCategory();

	void SetUnits(int u);
	int GetUnits();

	virtual void ComputeSupportMetric()=0; //metric is defined in derived classes
	
	int GetSupportMetric();
	void SetSupportMetric(int m);

	void DisplayModule();
}
; //note that ";" is required at end of class definition header file
#endif // MODULE_H
