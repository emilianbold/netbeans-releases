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

//Implementation of storage module class

#include "memory.h"
//Error logic needs to be added for SetDescription() and SetVendor() since these can return non-zero

Memory::Memory()
{
	SetDescription((char*)"Memory");
	SetVendor((char*)"generic"); //Placeholder for future functionality
	SetType(tstandard); //default memory speed is "standard"
	SetCategory (t1); //default is 1GB sub-module
	SetUnits(1); //default is "1" memory sub-module
	ComputeSupportMetric();
}

Memory::Memory(int type, int cat)
{
	SetDescription((char*)"Memory");
	SetVendor((char*)"generic"); //Placeholder for future functionality
	SetType(type);
	SetCategory (cat);
	SetUnits(1); //default is "1" memory sub-module
	ComputeSupportMetric();
}

Memory::Memory(int type, int cat, int units)
{
	SetDescription((char*)"Memory");
	SetVendor((char*)"generic"); //Placeholder for future functionality
	SetType(type);
	SetCategory (cat);
	SetUnits(units);
	ComputeSupportMetric();
}
//

void Memory::ComputeSupportMetric() //base class defines this function as "pure virtual"
{
//heuristic for memory module complexity is based on number of memory sub-modules and memory speed
//size of sub-module is not considered in heuristic
	int metric;
	metric=200*GetUnits();
	switch (GetType())
	{
		case tstandard: break;
		case tfast: metric += 100; break;
		case tultra: metric += 200; break;
		default: break;
	}
	SetSupportMetric(metric);
	
}

// end memory.cc
