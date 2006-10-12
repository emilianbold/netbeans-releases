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
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
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

    public NamedComponentReference<PortType> getType() {
        return resolveGlobalReference(PortType.class, WSDLAttribute.PORT_TYPE);
    }

    public void setType(NamedComponentReference<PortType> portType) {
        setAttribute(TYPE_PROPERTY, WSDLAttribute.PORT_TYPE, portType);
    }

    public void addExtensibilityElement(ExtensibilityElement ee) {
        addAfter(EXTENSIBILITY_ELEMENT_PROPERTY, ee, TypeCollection.DOCUMENTATION.types());
    }
    
    public void removeBindingOperation(BindingOperation bindingOperation) {
        removeChild(BINDING_OPERATION_PROPERTY, bindingOperation);
    }

    public void addBindingOperation(BindingOperation bindingOperation) {
        addAfter(BINDING_OPERATION_PROPERTY, bindingOperation, TypeCollection.DOCUMENTATION_EE.types());
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
