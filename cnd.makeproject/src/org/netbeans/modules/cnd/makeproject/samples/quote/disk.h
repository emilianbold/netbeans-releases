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

#if !defined DISK_H
#define DISK_H
#include "module.h"

enum tDisk{tsingle=1 , traid}; //type
enum tGB{t100=1, t200, t500}; //category represents disk sub-module size in GB


class Disk:public Module
{
public:
	Disk();
	Disk(int type, int size);
	Disk(int type, int size, int units);

//copy constructor derived unchanged from base class
//destructor derived unchanged from base class
	void ComputeSupportMetric();
}
; // note that ";" is required at end of class definition header file 

#endif // DISK_H
