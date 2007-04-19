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

package org.netbeans.modules.debugger.jpda.heapwalk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;

/**
 *
 * @author Martin Entlicher
 */
public class InstanceNumberCollector {
    
    private Map<JPDAClassType, long[]> classes = new HashMap<JPDAClassType, long[]>();
    
    /** Creates a new instance of InstanceNumberCollector */
    public InstanceNumberCollector() {
    }
    
    public int getInstanceNumber(ObjectVariable var) {
        JPDAClassType type = var.getClassType();
        if (type == null) {
            return 0;
        }
        long[] instancesID;
        synchronized (this) {
            instancesID = classes.get(type);
            if (instancesID == null) {
                List<ObjectVariable> instances = type.getInstances(0);
                int n = instances.size();
                instancesID = new long[n];
                for (int i = 0; i < n; i++) {
                    instancesID[i] = instances.get(i).getUniqueID();
                }
                classes.put(type, instancesID);
            }
        }
        long id = var.getUniqueID();
        int i;
        for (i = 0; i < instancesID.length; i++) {
            if (id == instancesID[i]) {
                break;
            }
        }
        return i + 1;
    }
    
}
