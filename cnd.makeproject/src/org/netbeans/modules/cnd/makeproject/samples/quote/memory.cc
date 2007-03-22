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

#include "memory.h"

Memory::Memory(int type /*= STANDARD */, int size /*= MEDIUM */, int units /*= 1 */) :
    Module("Memory", "generic", type, size, units) {
        ComputeSupportMetric();
    }
    
/*
 *  Heuristic for memory module complexity is based on number of memory 
 *  sub-modules and memory speed. 
 *
 *  Size of sub-module is not considered in heuristic.
 */

void Memory::ComputeSupportMetric() { 
    
    int metric = 200 * GetUnits();
        
    switch (GetTypeID()) {
        case FAST: 
            metric += 100; 
            break;
            
        case ULTRA: 
            metric += 200;
            break;
    }
        
    SetSupportMetric(metric);            
}
    
const char* Memory::GetType() const {
    switch (GetTypeID()) {
        case STANDARD: 
            return "Standard Memory";
            
        case FAST: 
            return "Fast Memory";
            
        case ULTRA: 
            return "UltraFast Memory";
            
        default: 
            return "Undefined";
    }
}

const char* Memory::GetCategory() const {
    switch (GetCategoryID()) {
        case SMALL:
            return "<= 1 Gb RAM";
            
        case MEDIUM: 
            return "1 - 2 Gb RAM";
            
        case BIG: return "4+ Gb RAM";
        
        default: 
            return "Undefined";
    }
}
  
// end memory.cc
