/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
