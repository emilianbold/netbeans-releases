/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.MaxLength;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This implements MaxLength interface represents the xs:maxLength facet.
 *
 * @author nn136682
 */
public class MaxLengthImpl extends CommonLength implements MaxLength {
    
    public MaxLengthImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.MAX_LENGTH,model));
    }
    
    /** Creates a new instance of MaxLengthImpl */
    public MaxLengthImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return MaxLength.class;
	}

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getComponentName() {
        return SchemaElements.MAX_LENGTH.getName();
    }
}
