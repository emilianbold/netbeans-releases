/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
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
    
    public void setType(GlobalReference<GlobalType> typeRef) {
        setSchemaReference(TYPE_PROPERTY, WSDLAttribute.TYPE, typeRef);
    }
    
     public GlobalReference<GlobalType> getType() {
        return resolveSchemaReference(GlobalType.class, WSDLAttribute.TYPE);
    }
    
     public void setElement(GlobalReference<GlobalElement> elementRef){
        setSchemaReference(ELEMENT_PROPERTY, WSDLAttribute.ELEMENT, elementRef);
    }
    
     public GlobalReference<GlobalElement> getElement() {
         return resolveSchemaReference(GlobalElement.class, WSDLAttribute.ELEMENT);
    }
    
}
