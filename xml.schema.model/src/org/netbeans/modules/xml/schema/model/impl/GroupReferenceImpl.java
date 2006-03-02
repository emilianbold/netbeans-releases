/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 * @author Chris Webster
 */
public class GroupReferenceImpl extends SchemaComponentImpl
	implements GroupReference {
    /**
     *
     */
    public GroupReferenceImpl(SchemaModelImpl model) {
	this(model,createNewComponent(SchemaElements.GROUP,model));
    }
    
    /**
     *
     */
    public GroupReferenceImpl(SchemaModelImpl model, Element e) {
	super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GroupReference.class;
	}
    
    /**
     *
     */
    public void accept(SchemaVisitor v) {
	v.visit(this);
    }
    
    /**
     *
     */
    public void setMinOccurs(Integer min) {
	setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, min);
    }
    
    /**
     *
     */
    public void setMaxOccurs(String max) {
	setAttribute(MAX_OCCURS_PROPERTY, SchemaAttributes.MAX_OCCURS, max);
    }
    
    /**
     *
     */
    public void setRef(GlobalReference<GlobalGroup> def) {
        setGlobalReference(REF_PROPERTY, SchemaAttributes.REF, def);
    }
    
    /**
     *
     */
    public GlobalReference<GlobalGroup> getRef() {
	return resolveGlobalReference(GlobalGroup.class, 
		SchemaAttributes.REF);
    }
    
    /**
     *
     */
    public Integer getMinOccurs() {
	String s = getAttribute(SchemaAttributes.MIN_OCCURS);
        return s == null ? null : Integer.valueOf(s);
    }
    
    public int getMinOccursEffective() {
        Integer v = getMinOccurs();
        return v == null ? getMinOccursDefault() : v;
    }

    public int getMinOccursDefault() {
        return 1;
    }

    /**
     *
     */
    public String getMaxOccurs() {
	return getAttribute(SchemaAttributes.MAX_OCCURS);
    }

    public String getMaxOccursEffective() {
        String s = getMaxOccurs();
        return s == null ? getMaxOccursDefault() : s;
    }

    public String getMaxOccursDefault() {
        return String.valueOf(1);
    }
}
