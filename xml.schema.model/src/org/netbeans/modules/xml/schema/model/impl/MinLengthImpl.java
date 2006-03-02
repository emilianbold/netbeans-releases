/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * MinLengthImpl.java
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.MinLength;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This implents interface represents the xs:minLength facet.
 * @author nn136682
 */
public class MinLengthImpl extends CommonLength implements MinLength {
    
    public MinLengthImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.MIN_LENGTH, model));
    }
    
    /** Creates a new instance of MinLengthImpl */
    public MinLengthImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return MinLength.class;
	}
    
    public String getComponentName() {
        return SchemaElements.MIN_LENGTH.getName();
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
}
