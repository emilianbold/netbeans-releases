/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
