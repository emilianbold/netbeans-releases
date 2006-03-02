/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 * TODO implement
 * @author Chris Webster
 */
public class LocalAttributeImpl extends LocalAttributeBaseImpl
	implements LocalAttribute {
    
    /**
     *
     */
    public LocalAttributeImpl(SchemaModelImpl model) {
	super(model,createNewComponent(SchemaElements.ATTRIBUTE, model));
    }
    
    /**
     *
     */
    public LocalAttributeImpl(SchemaModelImpl model, Element e) {
	super(model,e);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
	return LocalAttribute.class;
    }
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
    }
}
