/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * EnumerationImpl.java
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

public class EnumerationImpl extends SchemaComponentImpl implements Enumeration {
    
    public EnumerationImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.ENUMERATION, model));
    }
    
    /** Creates a new instance of EnumerationImpl */
    public EnumerationImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Enumeration.class;
	}

    public void setValue(String value) {
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, value);
    }

    public String getValue() {
        return getAttribute(SchemaAttributes.VALUE);
    }

    /**
     * Visitor providing
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
