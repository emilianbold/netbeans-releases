/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * KeyImpl.java
 *
 * Created on October 6, 2005, 2:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Key;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class KeyImpl extends ConstraintImpl implements Key {
    
    public KeyImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.KEY,model));
    }
    
    /**
     * Creates a new instance of KeyImpl
     */
    public KeyImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Key.class;
	}
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
