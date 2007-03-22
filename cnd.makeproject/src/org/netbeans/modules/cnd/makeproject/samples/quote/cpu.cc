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

// Implementation of CPU module class

#include "cpu.h"

Cpu::Cpu(int type /*= MEDIUM */, int architecture /*= OPTERON */, int units /*= 1*/) :
    Module("CPU", "generic", type, architecture, units) {
        ComputeSupportMetric();
}
    
/*
 * Heuristic for CPU module complexity is based on number of CPUs and
 * target use ("category"). CPU architecture ("type") is not considered in
 * heuristic
 */

void Cpu::ComputeSupportMetric() {
    int metric = 100 * GetUnits();

    switch (GetTypeID()) {
        case MEDIUM:
            metric += 100;
            break;

        case HIGH:
            metric += 400;
            break;
    }

    SetSupportMetric(metric);
}

const char* Cpu::GetType() const {
    switch (GetTypeID()) {
        case MEDIUM:
            return "Middle Class CPU";

        case HIGH:
            return "High Class CPU";

        default:
            return "Undefined";
    }
}

const char* Cpu::GetCategory() const {
    switch (GetCategoryID()) {
        case OPTERON:
            return "AMD Opteron Processor";

        case INTEL:
            return "Intel Processor";

        case SPARC:
            return "SUN Sparc Processor";

        default: return "Undefined";
    }
}

// end cpu.cc
