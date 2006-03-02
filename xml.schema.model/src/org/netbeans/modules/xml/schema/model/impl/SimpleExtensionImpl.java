/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * SimpleExtensionImpl.java
 *
 * Created on October 6, 2005, 9:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;

import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class SimpleExtensionImpl extends CommonExtensionImpl implements SimpleExtension{
    
    /** Creates a new instance of SimpleExtensionImpl */
    protected SimpleExtensionImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.EXTENSION, model));
    }
    
    public SimpleExtensionImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return SimpleExtension.class;
	}

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

   
}
