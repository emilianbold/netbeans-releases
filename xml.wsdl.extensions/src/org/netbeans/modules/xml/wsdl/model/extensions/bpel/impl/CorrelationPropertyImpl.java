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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.spi.NamedExtensibilityElementBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class CorrelationPropertyImpl extends NamedExtensibilityElementBase implements ReferenceableExtensibilityElement, CorrelationProperty {
    
    public CorrelationPropertyImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public CorrelationPropertyImpl(WSDLModel model){
        this(model, createPrefixedElement(BPELQName.PROPERTY.getQName(), model));
    }
    
    protected String getNamespaceURI() {
        return BPELQName.VARPROP_NS;
    }
    
    public NamedComponentReference<GlobalType> getType() {
        return resolveSchemaReference(GlobalType.class, BPELAttribute.TYPE);
    }

    public void setType(NamedComponentReference<GlobalType> type) {
        setAttribute(TYPE_PROPERTY, BPELAttribute.TYPE, type);
    }

    public void accept(BPELExtensibilityComponent.Visitor v) {
        v.visit(this);
    }

    public NamedComponentReference<GlobalElement> getElement() {
        return resolveSchemaReference(GlobalElement.class, BPELAttribute.ELEMENT);
    }

    public void setElement( NamedComponentReference<GlobalElement> value ) {
        setAttribute(ELEMENT_PROPERTY, BPELAttribute.ELEMENT, value);        
    }
}
