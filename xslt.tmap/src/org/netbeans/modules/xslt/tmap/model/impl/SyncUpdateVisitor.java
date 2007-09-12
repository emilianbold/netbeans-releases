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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class SyncUpdateVisitor implements ComponentUpdater<TMapComponent>, TMapVisitor {

    private TMapComponent myParent ;
    private int myIndex;
    private Operation myOperation;
    
    public SyncUpdateVisitor() {
    }

    public void update(TMapComponent target, TMapComponent child,
                       Operation operation) {
        update(target, child, -1, operation);
    }

    public void update(TMapComponent target, TMapComponent child, int index,
                       Operation operation) 
    {
        assert target != null;
        assert child != null;
        assert operation == Operation.ADD || operation == Operation.REMOVE;

        myParent = target;
        myIndex = index;
        myOperation = operation;
        
        child.accept(this);
    }

    public void visit(TransformMap transformMap) {
        assert false : "Should never add or remove transformmap root";
    }

    public void visit(Service service) {
        assert getParent() instanceof TransformMap;
        TransformMap transformMap = (TransformMap)getParent();
        if (isAdd()) {
            transformMap.addService(service);
        } else if (isRemove()) {
            transformMap.removeService(service);
        }
    }

    public void visit(org.netbeans.modules.xslt.tmap.model.api.Operation operation) {
        assert getParent() instanceof Service;
        Service service = (Service)getParent();
        if (isAdd()) {
            service.addOperation(operation);
        } else if (isRemove()) {
            service.removeOperation(operation);
        }
    }

    public void visit(Invoke invoke) {
        assert getParent() instanceof org.netbeans.modules.xslt.tmap.model.api.Operation;
        org.netbeans.modules.xslt.tmap.model.api.Operation operation 
                = (org.netbeans.modules.xslt.tmap.model.api.Operation)getParent();
        if (isAdd()) {
            operation.addInvoke(invoke);
        } else if (isRemove()) {
            operation.removeInvoke(invoke);
        }
    }

    public void visit(Transform transform) {
        assert getParent() instanceof org.netbeans.modules.xslt.tmap.model.api.Operation;
        org.netbeans.modules.xslt.tmap.model.api.Operation operation 
                = (org.netbeans.modules.xslt.tmap.model.api.Operation)getParent();
        if (isAdd()) {
            operation.addTransform(transform);
        } else if (isRemove()) {
            operation.removeTransforms(transform);
        }
    }

    public void visit(Param param) {
        assert getParent() instanceof Transform;
        Transform transform = (Transform)getParent();
        if (isAdd()) {
            transform.addParam(param);
        } else if (isRemove()) {
            transform.removeParam(param);
        }
    }

    private TMapComponent getParent() {
        return myParent;
    }
    
    private int getIndex() {
        return myIndex;
    }
    
    private Operation getOperation() {
        return myOperation;
    }
    
    private boolean isAdd() {
        return Operation.ADD == myOperation;
    }
    
    private boolean isRemove() {
        return Operation.REMOVE == myOperation;
    }

}
