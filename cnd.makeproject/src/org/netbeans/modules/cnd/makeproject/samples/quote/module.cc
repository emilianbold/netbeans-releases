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
//Implementation of base module class

#include <iostream>
#include <cstdlib>
#include <cstring>
using namespace std;

#include "module.h"

const int sz = 80; //built-in assumption that strings are 79 characters (+1 for ending null)


Module::Module()
{
	description = new char[sz];
	vendor = new char[sz]; //this anticipates future functionality
	strcpy(description, "undefined");
	strcpy(vendor, "undefined"); //this anticipates future functionality
}

Module::Module(const Module& obj) //copy constructor
{
	description = new char[sz];
	vendor = new char[sz];	
	
	strcpy(description, obj.description);
	strcpy(vendor, obj.vendor);
	type = obj.type;
	category = obj.category;
	units = obj.units;
	supportMetric = obj.supportMetric;
}

Module& Module::operator=(Module& obj)
{
	description = new char[sz];
	vendor = new char[sz];

	strcpy(description, obj.description);
	strcpy(vendor, obj.vendor);
	type = obj.type;
	category = obj.category;
	units = obj.units;
	supportMetric = obj.supportMetric;

	return *this;
}
	

Module::~Module()
{
	delete [] description;
	delete [] vendor; //anticipates future functionality
}

int Module::SetDescription(char* d)
{
	description = new char[sz];
	if ((strlen(d) + 1) > sz)
	{
		strcpy (description, "error");
		return 1; //error code ... description string too long
	}
	strcpy (description, d);
	return 0; //normal exit
}

char* Module::GetDescription()
{
	return description;
}

int Module::SetVendor(char* v)
{
	vendor = new char[sz];
	if ((strlen(v) + 1) > sz)
	{
		strcpy (vendor, "error");
		return 1; //error code ... vendor field too long
	}
	strcpy (vendor, v);
	return 0; //normal exit
}

char* Module::GetVendor()
{
	return vendor;
}

void Module::SetType(int t)
{
	type=t;
}

int Module::GetType()
{
	return type;
}

void Module::SetCategory(int c)
{
	category=c;
}

int Module::GetCategory()
{
	return category;
}

void Module::SetUnits(int u)
{
	units=u;
}

int Module::GetUnits()
{
	return units;
}

int Module::GetSupportMetric()
{
	return supportMetric;
}

void Module::SetSupportMetric(int m)
{
	supportMetric=m;
}

void Module::DisplayModule()
{
	cout<<"** "<<description<<" module data **"<<endl;
	cout<<"Module type: "<<type<<endl;
	cout<<"Module category: "<<category<<endl;
	cout<<"Number of sub-modules: "<<units<<endl;
	cout<<"Module support metric: "<<supportMetric<<endl;
	cout<<endl;
}


//end module.cc

