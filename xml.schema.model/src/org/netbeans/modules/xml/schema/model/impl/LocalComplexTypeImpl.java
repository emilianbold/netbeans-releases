/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * LocalComplexType.java
 *
 * Created on October 6, 2005, 9:03 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class LocalComplexTypeImpl extends CommonComplexTypeImpl implements LocalComplexType{
    
    /**
     * Creates a new instance of LocalComplexTypeImpl
     */
    protected LocalComplexTypeImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.COMPLEX_TYPE, model));
    }
    
    public LocalComplexTypeImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return LocalComplexType.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }    
}
