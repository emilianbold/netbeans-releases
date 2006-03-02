/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * SimpleTypeRestrictionImpl.java
 *
 * Created on October 6, 2005, 9:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class SimpleTypeRestrictionImpl extends CommonSimpleRestrictionImpl implements SimpleTypeRestriction{
    
    /** Creates a new instance of SimpleTypeRestrictionImpl */
    protected SimpleTypeRestrictionImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.RESTRICTION, model));
    }
    
    public SimpleTypeRestrictionImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return SimpleTypeRestriction.class;
	}

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

   
    
}
