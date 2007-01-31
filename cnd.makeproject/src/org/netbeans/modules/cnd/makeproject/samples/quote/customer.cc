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

#include "customer.h"

#include <cstdlib>
#include <cstring>
#include <iostream>
using namespace std;

Customer::Customer()
{
	int sz=80;
	customerName = new char[sz];
	strcpy(customerName, "unknown");
	discountCode=0; //default discount code is "0"
}

Customer::Customer(char* name)
{
	int sz=strlen(name) + 1;
	customerName = new char[sz];
	strcpy(customerName, name);
	discountCode=0; //default discount code is "0"
}

Customer::Customer(char* name, int discount)
{
	int sz=strlen(name) + 1;
	customerName = new char[sz];
	strcpy(customerName, name);
	discountCode=discount;
}

Customer::Customer(const Customer& obj) //copy constructor
{
	int sz=strlen(obj.customerName) + 1;
	customerName= new char[sz];
	strcpy(customerName, obj.customerName);
	discountCode = obj.discountCode;
}

Customer& Customer::operator=(Customer &obj) //overload assignment ("=") operator
{
	int sz=strlen(obj.customerName) + 1;
	customerName= new char[sz];
	strcpy(customerName, obj.customerName);
	discountCode = obj.discountCode;
	return *this;
}

Customer::~Customer()
{
	delete [] customerName;

}

char* Customer::GetCustomerName()
{
	return customerName;
}

int Customer::GetDiscountCode()
{
	return discountCode;
}

void Customer::DisplayCustomer()
{
	cout<<"** Customer details **"<<endl;
	cout<<"Customer name: "<<customerName<<endl;
	cout<<"Customer discount code: "<<discountCode<<endl<<endl;
}

// end of customer.cc
