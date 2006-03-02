/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * LocalSimpleTypeImpl.java
 *
 * Created on October 5, 2005, 6:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class LocalSimpleTypeImpl extends CommonSimpleTypeImpl implements LocalSimpleType{
    
    /** Creates a new instance of LocalSimpleTypeImpl */
    protected LocalSimpleTypeImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.SIMPLE_TYPE, model));
    }
    
    public LocalSimpleTypeImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return LocalSimpleType.class;
	}

    /**
     * Visitor providing
     */
    public void accept(org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor visitor) {
        visitor.visit(this);
    }


}
