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

import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingOutputImpl extends NamedImpl implements BindingOutput {
    
    /** Creates a new instance of BindingOutputImpl */
    public BindingOutputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingOutputImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.OUTPUT.getQName(), model));
    }

    public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setOutput(Reference<Output> Output) {
        assert false: "Reference to Output is read-only, use setName() instead.";
    }

    public Reference<Output> getOutput() {
        return new OutputReference(this);
    }
    
    static class OutputReference extends AbstractReference<Output> implements Reference<Output> {
        public OutputReference(BindingOutputImpl parent){
            super(Output.class, parent, parent.getName());
        }

        public BindingOutputImpl getParent() {
            return (BindingOutputImpl) super.getParent();
        }
        
        public String getRefString() {
            return getParent().getName();
        }

        public Output get() {
            if (getReferenced() == null) {
                BindingOperation bo = (BindingOperation) getParent().getParent();
                if (bo != null) {
                    if (bo.getOperation() != null) {
                        Operation op = bo.getOperation().get();
                        if (op != null) {
                            setReferenced(op.getOutput());
                        }
                    }
                }
            }           
            return getReferenced();
        }
        
    }
}
