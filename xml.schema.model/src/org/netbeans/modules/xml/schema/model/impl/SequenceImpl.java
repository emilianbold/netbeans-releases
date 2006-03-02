/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * TODO implement
 * @author Chris Webster
 */
public class SequenceImpl extends CommonSequenceImpl 
implements Sequence {
    public SequenceImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.SEQUENCE,model));
    }
    
    public SequenceImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Sequence.class;
	}

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    public void setMinOccurs(Integer min) {
        setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, min);
    }

    public void setMaxOccurs(String max) {
        setAttribute(MAX_OCCURS_PROPERTY, SchemaAttributes.MAX_OCCURS, max);
    }

    public Integer getMinOccurs() {
        String s = getAttribute(SchemaAttributes.MIN_OCCURS);
        return s == null ? null : Integer.valueOf(s);
    }

    public String getMaxOccurs() {
        return getAttribute(SchemaAttributes.MAX_OCCURS);
    }
    
    public int getMinOccursEffective() {
        Integer v = getMinOccurs();
        return v == null ? getMinOccursDefault() : v;
    }

    public int getMinOccursDefault() {
        return 1;
    }

    public String getMaxOccursEffective() {
        String v = getMaxOccurs();
        return v == null ? getMaxOccursDefault() : v;
    }

    public String getMaxOccursDefault() {
        return String.valueOf(1);
    }

}
