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

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class PartImpl extends NamedImpl implements Part {
    
    /** Creates a new instance of PartImpl */
    public PartImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public PartImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.PART.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public void setType(NamedComponentReference<GlobalType> typeRef) {
        setAttribute(TYPE_PROPERTY, WSDLAttribute.TYPE, typeRef);
    }
    
     public NamedComponentReference<GlobalType> getType() {
        return resolveSchemaReference(GlobalType.class, WSDLAttribute.TYPE);
    }
    
     public void setElement(NamedComponentReference<GlobalElement> elementRef){
        setAttribute(ELEMENT_PROPERTY, WSDLAttribute.ELEMENT, elementRef);
    }
    
     public NamedComponentReference<GlobalElement> getElement() {
         return resolveSchemaReference(GlobalElement.class, WSDLAttribute.ELEMENT);
    }
    
}
