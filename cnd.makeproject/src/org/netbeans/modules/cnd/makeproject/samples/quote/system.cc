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

//Implementation of System class: collection of modules

#include "system.h"
#include <iostream>
#include <assert.h>

System::System() :
    supportMetric(0) {
}

void System::AddModule(Module* module) {
    moduleList.push_back(module);
    supportMetric += module->GetSupportMetric();
}

Module& System::GetModule(int i) const {
    assert(i >= 0 && (unsigned)i < moduleList.size());
    
    return (*moduleList[i]);
}

int System::GetModuleCount() const {
    return moduleList.size();
}

int System::GetSupportMetric() const {
    return supportMetric;
};

ostream& operator <<(ostream& output, const System& system) {
    int size = system.GetModuleCount();
    
    output << "System consists of " << size << " module(s):" << endl << endl;
    
    for (int i = 0; i < size; i++) {
        output << system.GetModule(i) << endl;
    }
    
    return output;
}

// end system.cc
