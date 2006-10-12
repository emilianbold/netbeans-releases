/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AnyImpl.java
 *
 * Created on October 7, 2005, 8:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class AnyImpl extends CommonAnyImpl implements AnyElement {

    public AnyImpl(SchemaModelImpl model) {
	this(model,createNewComponent(SchemaElements.ANY,model));
    }
    
    /**
     * Creates a new instance of AnyImpl
     */
    public AnyImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return AnyElement.class;
	}
    
    /**
     *
     */
    public void setMinOccurs(Integer occurs) {
	setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, occurs);
    }
    
    /**
     *
     */
    public void setMaxOccurs(String occurs) {
	setAttribute(MAX_OCCURS_PROPERTY, SchemaAttributes.MAX_OCCURS, occurs);
    }
    
    /**
     *
     */
    public Integer getMinOccurs() {
        String v = getAttribute(SchemaAttributes.MIN_OCCURS);
	return v == null ? null : Integer.valueOf(v);
    }
    
    /**
     *
     */
    public String getMaxOccurs() {
	return getAttribute(SchemaAttributes.MAX_OCCURS);
    }
    
    public int getMinOccursDefault() {
        return 1;
    }
    
    public int getMinOccursEffective() {
        String s = getAttribute(SchemaAttributes.MIN_OCCURS);
        return s == null ? getMinOccursDefault() : Integer.valueOf(s).intValue();
    }
    
    public String getMaxOccursDefault() {
        return String.valueOf(1);
    }
    
    public String getMaxOccursEffective() {
        String s = getAttribute(SchemaAttributes.MAX_OCCURS);
        return s == null ? getMaxOccursDefault() : s;
    }
    
    /**
     *
     */
    public void accept(SchemaVisitor v) {
	v.visit(this);
    }
}