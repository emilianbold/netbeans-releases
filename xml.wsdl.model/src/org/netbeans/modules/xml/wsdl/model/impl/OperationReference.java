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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author Nam Nguyen
 */
public class OperationReference extends AbstractReference<Operation> implements Reference<Operation> {
    
    /** Creates a new instance of OperationReference */
    public OperationReference(Operation referenced, AbstractComponent parent) {
        super(referenced, Operation.class, parent);
    }
    
    //used by resolve methods
    public OperationReference(AbstractComponent parent, String ref){
        super(Operation.class, parent, ref);
    }
    
    public String getRefString() {
        if (refString == null) {
            refString = getReferenced().getName();
        }
        return refString;
    }
    
    public Operation get() {
        if (getReferenced() != null) return getReferenced();
        
        String operationName = getRefString();
        if (operationName == null) {
            return null;
        }
        
        Binding p = (Binding) getParent().getParent();
        if (p == null || p.getType() == null) {
            return null;
        }
        
        PortType pt = p.getType().get();
        if (pt == null || pt.getOperations() == null) {
            return null;
        }
        
        Collection<Operation> operations = pt.getOperations();
        BindingOperation bindingOp = (BindingOperation) getParent();
        List<Operation> candidates = new ArrayList<Operation>();
        for (Operation op : operations) {
            if (operationName.equals(op.getName())) {
                candidates.add(op);
            }
        }
        
        // find perfect matched
        for (Operation op : candidates) {
            BindingInput bi = bindingOp.getBindingInput();
            BindingOutput bo = bindingOp.getBindingOutput();
            Input in = op.getInput();
            Output out = op.getOutput();
            if (in == null && bi == null && out != null && bo != null && out.getName().equals(bo.getName()) ||
                out == null && bo == null && in != null && bi != null && in.getName().equals(bi.getName()) ||
                in != null && bi != null && out != null && bo != null && 
                in.getName().equals(bi.getName()) && out.getName().equals(bo.getName()))
            {
                setReferenced(op);
                break;
            }
        }
        
        if (getReferenced() == null && ! candidates.isEmpty()) {
            setReferenced(candidates.get(0));
        }
        return getReferenced();
    }
}
