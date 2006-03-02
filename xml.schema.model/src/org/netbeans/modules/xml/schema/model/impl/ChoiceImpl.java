/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * TODO implement
 * @author Chris Webster
 */
public class ChoiceImpl  extends CommonChoiceImpl implements Choice {
    
    public ChoiceImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.CHOICE,model));
    }
    
    public ChoiceImpl(SchemaModelImpl model, Element e) {
	super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Choice.class;
	}
    
    public void setMaxOccurs(String max) {
	setAttribute(MAX_OCCURS_PROPERTY, SchemaAttributes.MAX_OCCURS, max);
    }
  
    public void setMinOccurs(Integer min) {
	setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, min);
    }
    
    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    public String getMaxOccurs() {
        return getAttribute(SchemaAttributes.MAX_OCCURS);
    }

    public Integer getMinOccurs() {
        String s = getAttribute(SchemaAttributes.MIN_OCCURS);
        return s == null ? null : Integer.valueOf(s);
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
}
