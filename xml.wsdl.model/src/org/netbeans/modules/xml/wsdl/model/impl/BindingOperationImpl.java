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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingOperationImpl extends WSDLComponentBase implements BindingOperation {
    
    /** Creates a new instance of BindingOperationImpl */
    public BindingOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingOperationImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.OPERATION.getQName(), model));
    }
    
    public void addExtensibilityElement(ExtensibilityElement ee) {
        addAfter(EXTENSIBILITY_ELEMENT_PROPERTY, ee, TypeCollection.DOCUMENTATION.types());
    }
    
    public void setBindingInput(BindingInput bindingInput) {
        setChildAfter(BindingInput.class, BINDING_INPUT_PROPERTY, bindingInput, TypeCollection.DOCUMENTATION_EE.types());
    }
    
    public BindingInput getBindingInput() {
        return getChild(BindingInput.class);
    }
    
    public void setBindingOutput(BindingOutput bindingOutput) {
        setChildAfter(BindingOutput.class, BINDING_OUTPUT_PROPERTY, bindingOutput, 
                TypeCollection.DOCUMENTATION_EXTENSIBILITY_BINDINGINPUT.types());
    }
    
    public BindingOutput getBindingOutput() {
        return getChild(BindingOutput.class);
    }
    
    public void setOperation(Reference<Operation> operationRef) {
        setName(operationRef == null ? null : operationRef.get().getName());
    }
    
    public Reference<Operation> getOperation() {
        return getName() == null ? null : new OperationReference(this, getName());
    }
    
    public void addBindingFault(BindingFault bindingFault) {
        addAfter(BINDING_FAULT_PROPERTY, bindingFault, TypeCollection.DOCUMENTATION_EXTENSIBILITY_BINDINGOUTPUT.types());
    }
    
    public void removeBindingFault(BindingFault bindingFault) {
        removeChild(BINDING_FAULT_PROPERTY, bindingFault);
    }
    
    public Collection<BindingFault> getBindingFaults() {
        return getChildren(BindingFault.class);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, WSDLAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(WSDLAttribute.NAME);
    }
}
