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

#include "cpu.h"
#include "disk.h"
#include "memory.h"
#include "system.h"
#include "customer.h"
#include "namelist.h"
#include <cstdlib>
#include <iostream>
#include <cstring>
using namespace std;

NameList* NameList::pList = 0; //Initialize NameList pointer to null. NameList has not yet been created.
NameList* pMasterNameList = NameList::ListInstance(); //Create MasterNameList

int main(int argc, char* argv[])
{
	const int sz = 80; //default maximum string is 79 characters (plus 1 for terminating null)

	char inputdata[sz];

	char customerName[sz];
	int index; //if customer is known set to index of customer in NameList, otherwise "-1"
	int type;
	int units;
	int discount;
	int metric;

	cout<<"Support metric quote program"<<endl<<endl;

//display known customer names
	(*pMasterNameList).DisplayList();

//when customer is not specified as program argument, prompt user for name
	if (argc != 2)
	{	cout<< "Enter customer name: ";
		cin>> customerName;
		cout<<endl;
	}
	else
	{
	if ((strlen(argv[1])+1)>sz)
		strcpy(customerName, "error"); //customer name more than 80 characters ... error
	else strcpy(customerName, argv[1]);		
	}

//Determine if customer is "known"
	index=(*pMasterNameList).FindCustomer(customerName);

	if (index >=0)
		discount=(*pMasterNameList).GetDiscount(index);
		
	else
		discount=0; //customer unknown ... default discount code is "0"
		
//Create customer object
	Customer MyCustomer(customerName, discount);

//Display customer characteristics
	MyCustomer.DisplayCustomer();

//Define system collection .. this is the list of modules
	System MySystem;	

//prompt user for cpu module details
	cout<<"Enter CPU module type M for middle, E for high: ";
	cin>>inputdata;
	cout<<endl;

	switch (inputdata[0])
	{
		case 'M': type=tmid; break;
		case 'm': type=tmid; break;
		case 'E': type=thigh; break;
		case 'e': type=thigh;break;
		case 'Q': return 2; //premature user requested termination
		case 'q': return 2; //default user requested termination
		default: type=tmid;
	}

	cout<<"Enter number of CPUs: ";
	cin>>units;
	cout<<endl;

	if (units <=0 || units >10) units=1;

	Cpu MyCpu(type,0,units); //Create CPU module object
	MySystem.AddModule(&MyCpu); //Add CPU Module to system specification


	//prompt user for disk module details
	cout<<"Enter disk module type: S for single disks R for RAID: ";
	cin>>inputdata;
	cout<<endl;

	switch (inputdata[0])
	{
		case 'S': type=tsingle; break;
		case 's': type=tsingle; break;
		case 'R': type=traid; break;
		case 'r': type=traid; break;
		case 'Q': return 2; //premature user requested termination
		case 'q': return 2; //premature user requested termination
		default: type=tsingle;
	}
	
	cout<<"Enter number of disks: ";
	cin>>units;
	cout<<endl;

	if (units <=0 || units >10) units=1;

	Disk MyDisk(type, 0, units); //Create disk module object
	MySystem.AddModule(&MyDisk); //Add Disk Module to system specification

	
	//prompt user for memory module details
	cout<<"Enter memory module type: S for standard, F for fast, U for ultra: ";
	cin>>inputdata;
	cout<<endl;

	switch (inputdata[0])
	{
		case 'S': type=tstandard; break;
		case 's': type=tstandard;break;
		case 'F': type=tfast; break;
		case 'f': type=tfast; break;
		case 'U': type=tultra; break;
		case 'u': type=tultra;break;
		case 'Q': return 2; //premature user requested termination
		case 'q': return 2; //premature user requested termination
		default: type=tstandard;
	}
	
	cout<<"Enter number of memory sub-modules: ";
	cin>>units;
	cout<<endl;

	if (units <=0 || units >10) units=1;

	Memory MyMemory(type,0,units); //Create memory module object
	MySystem.AddModule(&MyMemory); //Add Memory Module to system specification

// end of system specification

// summarize system specification
	metric=MySystem.GetSupportMetric();
	MySystem.DisplayList();
// end of system specification
	
	discount = MyCustomer.GetDiscountCode();
	cout<< "Quote support Metric: "<<metric<<endl; 
	cout<< "Quote discount code: "<<discount<<endl;

	cout<<endl<< "To quit enter Q: ";
	cin>> inputdata;
	cout<< endl;

	if (inputdata[0] != 'Q') return 1;
	else return 0;

}

//end main.cc
