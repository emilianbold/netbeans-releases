/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.MaxInclusive;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This implements interface represents the xs:maxInclusive facet.
 *
 * @author nn136682
 */
public class MaxInclusiveImpl extends BoundaryElement implements MaxInclusive {
    
    public MaxInclusiveImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.MAX_INCLUSIVE, model));
    }
    
    /** Creates a new instance of MaxInclusiveImpl */
    public MaxInclusiveImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return MaxInclusive.class;
	}

    public String getComponentName() {
        return SchemaElements.MAX_INCLUSIVE.getName();
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }    
}
