/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * UniqueImpl.java
 *
 * Created on October 6, 2005, 2:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Unique;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 * @author rico
 */
public class UniqueImpl extends ConstraintImpl implements Unique {
    
    /**
     *
     */
    public UniqueImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.UNIQUE,model));
    }
    
    /**
     *
     */
    public UniqueImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Unique.class;
	}
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
