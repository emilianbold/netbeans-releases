/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This implements interface represents xs:pattern used to define regular expressions over 
 * the lexical space.
 *
 * @author nn136682
 */
public class PatternImpl extends SchemaComponentImpl implements Pattern {
    
    public PatternImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.PATTERN, model));
    }
    
    /** Creates a new instance of PatternImpl */
    public PatternImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Pattern.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    

    public String getValue() {
        return getAttribute(SchemaAttributes.VALUE);
    }
	
    public void setValue(String value) {
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, value);
    }
}

