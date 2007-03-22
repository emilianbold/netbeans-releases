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

#ifndef DISK_H
#define DISK_H
#include "module.h"

class Disk : public Module {
public:
    enum DiskType { SINGLE,  RAID }; 
    enum DiskCapacity { T100,  T200, T500 }; // category represents disk sub-module size in GB

    Disk(int type = SINGLE, int size = T100, int units =1);
    virtual const char* GetType() const;
    virtual const char* GetCategory() const;
        
protected:        
    void ComputeSupportMetric();
};

#endif // DISK_H
