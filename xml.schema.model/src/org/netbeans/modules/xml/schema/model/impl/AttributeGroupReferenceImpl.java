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
 * TODO implement
 * @author Chris Webster
 */
public class AttributeGroupReferenceImpl extends SchemaComponentImpl
implements AttributeGroupReference {
     public AttributeGroupReferenceImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.ATTRIBUTE_GROUP,model));
    }
    
    public AttributeGroupReferenceImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return AttributeGroupReference.class;
	}

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    public void setGroup(GlobalReference<GlobalAttributeGroup> group) {
	setGlobalReference(GROUP_PROPERTY, SchemaAttributes.REF, group);
    }

    public GlobalReference<GlobalAttributeGroup> getGroup() {
        return resolveGlobalReference(GlobalAttributeGroup.class, SchemaAttributes.REF);
 
    }
}
