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

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
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
    public void setRef(NamedComponentReference<GlobalGroup> def) {
        setAttribute(REF_PROPERTY, SchemaAttributes.REF, def);
    }
    
    /**
     *
     */
    public NamedComponentReference<GlobalGroup> getRef() {
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
