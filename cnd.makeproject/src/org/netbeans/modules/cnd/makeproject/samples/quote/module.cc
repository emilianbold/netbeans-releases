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
//Implementation of base module class

#include <iostream>
#include <cstdlib>
#include "module.h"

Module::Module() {
    SetDescription("undefined");
    SetVendor("undefined");
}

Module::Module(const char* _description, const char* _vendor, int _type, int _category, int _units) {
    SetDescription(_description);
    SetVendor(_vendor);
    SetType(_type);
    SetCategory(_category);
    SetUnits(_units);
}

//copy constructor
Module::Module(const Module& obj) {
    SetDescription(obj.GetDescription());
    SetVendor(obj.GetVendor());
    SetType(obj.type);
    SetUnits(obj.units);
    SetSupportMetric(obj.GetSupportMetric());
}

Module& Module::operator= (const Module& obj) {
    SetDescription(obj.GetDescription());
    SetVendor(obj.GetVendor());
    SetType(obj.type);
    SetUnits(obj.units);
    SetSupportMetric(obj.GetSupportMetric());
    
    return *this;
}

Module::~Module() {
}

void Module::SetDescription(const char* new_description) {
    description = new_description;
}

const char* Module::GetDescription() const {
    return description.c_str();
}

void Module::SetVendor(const char* new_vendor) {
    vendor = new_vendor;
}

const char* Module::GetVendor() const {
    return vendor.c_str();
}

void Module::SetType(int new_type) {
    type = new_type;
}

int Module::GetTypeID() const {
    return type;
}

void Module::SetCategory(int new_category) {
    category = new_category;
}

int Module::GetCategoryID() const {
    return category;
}

void Module::SetUnits(int new_units) {
    units = new_units;
}

int Module::GetUnits() const {
    return units;
}

void Module::SetSupportMetric(int new_metric) {
    supportMetric = new_metric;
}

int Module::GetSupportMetric() const {
    return supportMetric;
}

ostream& operator <<(ostream& output, const Module& module) {
    
    output << "** " << module.GetDescription() << " module data **" << endl;
    output << "Module type: " << module.GetType() << endl;
    output << "Module category: " << module.GetCategory() << endl;
    output << "Number of sub-modules: " << module.GetUnits() << endl;
    output << "Module support metric: " << module.GetSupportMetric() << endl;
    
    return output;
}
 
// end module.cc

