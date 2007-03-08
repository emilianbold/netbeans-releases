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

//Implementation of CPU module class

#include "cpu.h"
//Error logic needs to be added for SetDescription() and SetVendor() since these can return non-zero

Cpu::Cpu() {
    SetDescription((char*)"CPU");
    SetVendor((char*)"generic"); //Placeholder for future functionality
    SetType(tmid);
    SetCategory(tOpteron); //Default architecture type in example
    SetUnits(1); //Default CPU board has single processor
    ComputeSupportMetric();
}

Cpu::Cpu(int ty, int cat) {
    SetDescription((char*)"CPU");
    SetVendor((char*)"generic"); //Placeholder for future functionality
    SetType(ty);
    SetCategory(cat);
    SetUnits(1); //Default CPU board has single processor
    ComputeSupportMetric();
}

Cpu::Cpu(int ty, int cat, int un) {
    SetDescription((char*)"CPU");
    SetVendor((char*)"generic"); //Placeholder for future functionality
    SetType(ty);
    SetCategory(cat);
    SetUnits(un);
    ComputeSupportMetric();
}

void Cpu::ComputeSupportMetric() //base class defines this function as "pure virtual"
{
//heuristic for CPU module complexity is based on number of CPU processors and target use ("category")
//CPU architecture ("type") is not considered in heuristic
    int metric;
    metric=100*GetUnits();
    switch (GetType()) {
        case tmid: metric += 100; break;
        case thigh: metric += 400; break;
        default: break;
    }
    SetSupportMetric(metric);
}

// end cpu.cc
