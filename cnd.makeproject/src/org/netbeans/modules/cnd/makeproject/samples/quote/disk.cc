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

//Implementation of disk module class

#include "disk.h"
//Error logic needs to be added for SetDescription() and SetVendor() since these can return non-zero

Disk::Disk() {
    SetDescription((char*)"Disk storage");
    SetVendor((char*)"generic"); //Placeholder for future functionality
    SetType(tsingle); //default architecture is "no redundantcy"
    SetCategory(t100); //default disk sub-module size is 100GB
    SetUnits(1); //default number of disk sub-modules is 1
    ComputeSupportMetric();
}

Disk::Disk(int ty, int cat) {
    SetDescription((char*)"Disk storage");
    SetVendor((char*)"generic"); //Placeholder for future functionality
    SetType(ty);
    SetCategory(cat);
    SetUnits(1); //default number of disk sub-modules is 1
    ComputeSupportMetric();
}

Disk::Disk(int ty, int cat, int un) {
    SetDescription((char*)"Disk storage");
    SetVendor((char*)"generic"); //Placeholder for future functionality
    SetType(ty);
    SetCategory(cat);
    SetUnits(un);
    ComputeSupportMetric();
}
//

void Disk::ComputeSupportMetric() //base class defines this function as "pure virtual"
{
//heuristic for disk module complexity is based on number of disk sub-modules and architecture
//size of individual disks is not considered in heuristic
    int metric;
    metric=200*GetUnits();
    switch (GetType()) {
        case tsingle: break;
        case traid: metric += 500; break;
        default: break;
    }
    SetSupportMetric(metric);
}

// end disk.cc
