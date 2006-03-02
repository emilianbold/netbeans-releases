/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * MinExclusiveImpl.java
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.MinExclusive;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This implements interface represents the xs:minExclusive facet.
 *
 * @author nn136682
 */
public class MinExclusiveImpl extends BoundaryElement implements MinExclusive {
    
    public MinExclusiveImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.MIN_EXCLUSIVE,model));
    }
    
    /** Creates a new instance of MinExclusiveImpl */
    public MinExclusiveImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return MinExclusive.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getComponentName() {
        return SchemaElements.MIN_EXCLUSIVE.getName();
    }
}
