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
package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author Nam Nguyen
 */
public class FaultReference extends AbstractReference<Fault> implements Reference<Fault> {
    
    public FaultReference(Fault referenced, AbstractDocumentComponent parent) {
        super(referenced, Fault.class, parent);
    }
    
    //used by resolve methods
    public FaultReference(AbstractDocumentComponent parent, String ref){
        super(Fault.class, parent, ref);
    }
    
    public String getRefString() {
        if (refString == null) {
            refString = getReferenced().getName();
        }
        return refString;
    }

    private SOAPFault getSOAPFault() {
        return (SOAPFault) getParent();
    }
    
    public Fault get() {
        if (getReferenced() == null) {
            if (getSOAPFault().getParent() == null || 
                getSOAPFault().getParent().getParent() == null) {
                return null;
            }
            BindingOperation bindingOp = (BindingOperation) getSOAPFault().getParent().getParent();
            Reference<Operation> ref = bindingOp.getOperation();
            Operation op = (ref == null) ? null : ref.get();
            if (op != null) {
                for (Fault f : op.getFaults()) {
                    if (refString.equals(f.getName())) {
                        setReferenced(f);
                        break;
                    }
                }
            }
        }
        return getReferenced();
    }
}
