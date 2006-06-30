/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
 * WhitespaceImpl.java
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Whitespace;
import org.netbeans.modules.xml.schema.model.Whitespace.Treatment;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public class WhitespaceImpl extends SchemaComponentImpl implements Whitespace{
    
    public WhitespaceImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.WHITESPACE,model));
    }
    
    /** Creates a new instance of WhitespaceImpl */
    public WhitespaceImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Whitespace.class;
	}
    
    public Boolean isFixed() {
        String v = getAttribute(SchemaAttributes.FIXED);
        return v == null ? null : Boolean.valueOf(v);
    }
    
    public void setFixed(Boolean isFixed) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, isFixed);
    }
    
    public boolean getFixedEffective() {
        Boolean v = isFixed();
        return v == null ? getFixedDefault() : v;
    }

    public boolean getFixedDefault() {
        return false;
    }
    
    public Treatment getValue() {
        String s = this.getAttribute(SchemaAttributes.VALUE);
        return s == null ? null : Util.parse(Treatment.class, s);
    }
    
    public void setValue(Treatment whitespaceTreatment) {
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, whitespaceTreatment);
    }

    /**
     * Visitor providing
     */
    public void accept(org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor visitor) {
        visitor.visit(this);
    }

    protected Class getAttributeType(SchemaAttributes attr) {
        switch(attr) {
            case VALUE:
                return Treatment.class;
            default:
                return super.getAttributeType(attr);
        }
    }

}
