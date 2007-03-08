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
using namespace std;

System::System() {
    supportMetric=0;
}

System::System(const System& obj) {
}

System& System::operator=(System& obj) {
    return *this;
}

System::~System() {
    moduleList.clear();
}

void System::AddModule(Module* m) {
    moduleList.push_back(m);
    supportMetric +=(*m).GetSupportMetric();
}

Module* System::GetModule(int i) {
    if (i >= 0 && i < (int)moduleList.size())
        return moduleList[i];
    else
        return (Module*)0;
}

int System::GetModuleCount() {
    return moduleList.size();
}

int System::GetSupportMetric() {
    return supportMetric;
};

void System::DisplayList() {
    Module* m;
    int sz=moduleList.size();
    cout<<"System consists of "<<sz<<" module(s)"<<endl;
    
    for (int i=0; i<sz; i++) {
        m=moduleList[i];
        (*m).DisplayModule();
    }
    
}

// end system.cc
