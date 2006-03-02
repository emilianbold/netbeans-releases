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
 *
 * @author Chris Webster
 */
public class AnyAttributeImpl extends CommonAnyImpl implements AnyAttribute {
    public AnyAttributeImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.ATTRIBUTE,model));
    }
    
    public AnyAttributeImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return AnyAttribute.class;
	}

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }
}
