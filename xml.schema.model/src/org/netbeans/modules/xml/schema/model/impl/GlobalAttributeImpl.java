/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * TODO implement
 * @author Chris Webster
 */
public class GlobalAttributeImpl extends CommonAttributeImpl 
    implements GlobalAttribute {
     public GlobalAttributeImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.ATTRIBUTE,model));
    }
    
    public GlobalAttributeImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalAttribute.class;
	}
    
    public void accept(SchemaVisitor v) {
        v.visit(this);
    }
    
}
