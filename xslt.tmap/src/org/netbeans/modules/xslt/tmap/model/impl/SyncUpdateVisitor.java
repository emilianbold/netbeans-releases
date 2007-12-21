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
