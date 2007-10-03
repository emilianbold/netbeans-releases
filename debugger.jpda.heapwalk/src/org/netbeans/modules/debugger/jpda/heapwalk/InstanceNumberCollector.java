/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        long id = var.getUniqueID();
        if (id == 0L) {
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
        int i;
        for (i = 0; i < instancesID.length; i++) {
            if (id == instancesID[i]) {
                break;
            }
        }
        return i + 1;
    }
    
}
