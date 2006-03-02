/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class BindingImpl extends WSDLComponentBase implements Binding {
    
    /** Creates a new instance of BindingImpl */
    public BindingImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.BINDING.getQName(), model));
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public GlobalReference<PortType> getType() {
        return resolveGlobalReference(PortType.class, WSDLAttribute.PORT_TYPE);
    }

    public void setType(GlobalReference<PortType> portType) {
        setGlobalReference(TYPE_PROPERTY, WSDLAttribute.PORT_TYPE, portType);
    }

    public void removeBindingOperation(BindingOperation bindingOperation) {
        removeChild(BINDING_OPERATION_PROPERTY, bindingOperation);
    }

    public void addBindingOperation(BindingOperation bindingOperation) {
        addAfter(BINDING_OPERATION_PROPERTY, bindingOperation, TypeCollection.DOCUMENTATION.types());
    }

    public Collection<BindingOperation> getBindingOperations() {
        return getChildren(BindingOperation.class);
    }

    
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, WSDLAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(WSDLAttribute.NAME);
    }
}
