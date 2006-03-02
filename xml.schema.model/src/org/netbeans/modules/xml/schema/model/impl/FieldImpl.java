/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * FieldImpl.java
 *
 * Created on October 6, 2005, 2:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class FieldImpl extends SchemaComponentImpl implements Field {
    
    public FieldImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.FIELD,model));
    }
    
    /**
     * Creates a new instance of FieldImpl
     */
    public FieldImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Field.class;
	}
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     *
     */
    public void setXPath(String xpath) {
        setAttribute(XPATH_PROPERTY, SchemaAttributes.NAME, xpath);
    }
    
    /**
     *
     */
    public String getXPath() {
        return getAttribute(SchemaAttributes.XPATH);
    }
}
