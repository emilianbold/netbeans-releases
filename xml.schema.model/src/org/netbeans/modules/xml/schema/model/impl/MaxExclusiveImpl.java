/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * MaxExclusiveImpl.java
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.MaxExclusive;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;


public class MaxExclusiveImpl extends BoundaryElement implements MaxExclusive {
    
    public MaxExclusiveImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.MAX_EXCLUSIVE,model));
    }
    
    /** Creates a new instance of MaxExclusiveImpl */
    public MaxExclusiveImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return MaxExclusive.class;
	}
    
    public String getComponentName() {
        return SchemaElements.MAX_EXCLUSIVE.getName();
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
