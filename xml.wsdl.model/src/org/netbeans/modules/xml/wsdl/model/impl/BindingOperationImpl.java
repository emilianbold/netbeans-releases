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
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
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
    
    public void setBindingInput(BindingInput bindingInput) {
        setChildAfter(BindingInput.class, BINDING_INPUT_PROPERTY, bindingInput, TypeCollection.DOCUMENTATION.types());
    }
    public BindingInput getBindingInput() {
        return getChild(BindingInput.class);
    }
    
    public void setBindingOutput(BindingOutput bindingOutput) {
        setChildAfter(BindingOutput.class, BINDING_OUTPUT_PROPERTY, bindingOutput, 
                TypeCollection.DOCUMENTATION_EXTENSIBILITY_BINDINDINPUT.types());
    }
    public BindingOutput getBindingOutput() {
        return getChild(BindingOutput.class);
    }
    
    public void setOperation(Operation operation) {
        String name = operation.getName();
        setAttribute(BINDING_OPERATION_PROPERTY, WSDLAttribute.NAME, name);
    }
    
    public Operation getOperation() {
        String name = getName();
        WSDLComponent p = getParent();
        assert(p instanceof Binding);
        Binding parent = Binding.class.cast(p);
        PortType pt = parent.getType().get();
        Collection<Operation> operations = pt.getOperations();
        for (Operation op : operations) {
            if (name.equals(op.getName())) {
                BindingInput bi = getBindingInput();
                BindingOutput bo = getBindingOutput();
                Input in = op.getInput();
                Output out = op.getOutput();
                if (bi != null && in != null && bi.getName().equals(in.getName()) ||
                    bi == null && in == null || bo == null && out == null ||
                    bo != null && out != null && bo.getName().equals(out.getName())) {
                    return op;
                }
            }
        }
        return null;
    }
    
    public void addBindingFault(BindingFault bindingFault) {
        addAfter(BINDING_FAULT_PROPERTY, bindingFault, TypeCollection.DOCUMENTATION.types());
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
