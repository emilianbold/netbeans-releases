/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * @author Chris Webster
 */
public class AttributeReferenceImpl extends LocalAttributeBaseImpl
	implements AttributeReference {
    
    /**
     *
     */
    public AttributeReferenceImpl(SchemaModelImpl model) {
	super(model,createNewComponent(SchemaElements.ATTRIBUTE, model));
    }
    
    /**
     *
     */
    public AttributeReferenceImpl(SchemaModelImpl model, Element e) {
	super(model,e);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
	return AttributeReference.class;
    }
    
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
    }
}
