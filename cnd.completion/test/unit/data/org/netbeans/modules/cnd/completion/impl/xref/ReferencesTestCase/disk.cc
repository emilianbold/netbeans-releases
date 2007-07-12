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

Disk::Disk(int type /*= SINGLE */, int size /*= T100 */, int units /*= 1 */) :
    Module("Disk storage", "generic", type, size, units) {
        ComputeSupportMetric();
}
    
/*
 * Heuristic for disk module complexity is based on number of disk sub-modules 
 * and architecture. Size of individual disks is not considered in heuristic
 */

void Disk::ComputeSupportMetric() { 
    int metric = 200 * GetUnits();
     
    if (GetTypeID() == RAID) {
        metric += 500;
    }
        
    SetSupportMetric(metric);
}
    
const char* Disk::GetType() const {
    switch (GetTypeID()) {
        case SINGLE: 
            return "Single disk";
        
        case RAID: 
            return "Raid";
         
        default: 
            return "Undefined";
    }
}
    
const char* Disk::GetCategory() const {
    switch (GetCategoryID()) {
        case T100: 
            return "100 Gb disk";
        
        case T200: 
            return "200 Gb disk";
        
        case T500: 
            return "500 Gb or more";
            
        default: 
            return "Undefined";
    }
}
    
// end disk.cc
