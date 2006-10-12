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

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingInputImpl extends NamedImpl implements BindingInput {
    
    /** Creates a new instance of BindingInputImpl */
    public BindingInputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingInputImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.INPUT.getQName(), model));
    }

    public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public void setInput(Reference<Input> input) {
        assert false: "reference to Input is read-only, use setName()";
    }

    // Note: input name is optional and only needs to be explicit when 
    // binding operation name is ambiguous.
    public Reference<Input> getInput() {
        return new InputReference(this);
    }
    
    static class InputReference extends AbstractReference<Input> implements Reference<Input> {
        public InputReference(BindingInputImpl parent){
            super(Input.class, parent, parent.getName());
        }

        public BindingInputImpl getParent() {
            return (BindingInputImpl) super.getParent();
        }
        
        public String getRefString() {
            return getParent().getName();
        }

        public Input get() {
            if (getReferenced() == null) {
                BindingOperation bo = (BindingOperation) getParent().getParent();
                if (bo != null) {
                    Operation op = bo.getOperation().get();
                    if (bo.getOperation() != null) {
                        if (op != null) {
                            setReferenced(op.getInput());
                        }
                    }
                }
            }
            return getReferenced();
        }
    }
    
}
