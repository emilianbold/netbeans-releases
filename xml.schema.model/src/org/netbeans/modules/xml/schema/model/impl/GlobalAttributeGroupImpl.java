/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * TODO implement
 * @author Chris Webster
 */
public class GlobalAttributeGroupImpl extends NamedImpl
    implements GlobalAttributeGroup {
    
     public GlobalAttributeGroupImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.ATTRIBUTE_GROUP,model));
    }
    
    public GlobalAttributeGroupImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalAttributeGroup.class;
	}

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    /**
     *
     */
    public void removeLocalAttribute(LocalAttribute attr) {
        removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }

    /**
     *
     */
    public void addLocalAttribute(LocalAttribute attr) {
        List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(LOCAL_ATTRIBUTE_PROPERTY, attr, list);
    }
    
     /**
     *
     */
    public void removeAttributeReference(AttributeReference attr) {
        removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }

    /**
     *
     */
    public void addAttributeReference(AttributeReference attr) {
        List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(LOCAL_ATTRIBUTE_PROPERTY, attr, list);
    }

    /**
     *
     */
    public void setAnyAttribute(AnyAttribute attr) {
        List<Class<? extends SchemaComponent>> list = Collections.emptyList();
        setChild(AnyAttribute.class, ANY_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    /**
     *
     */
    public void removeAttributeGroupReference(AttributeGroupReference ref) {
        removeChild(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref);
    }

    /**
     *
     */
    public void addAttributeGroupReference(AttributeGroupReference ref) {
        List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref, list);
    }

    /**
     *
     */
    public Collection<AttributeGroupReference> getAttributeGroupReferences() {
        return getChildren(AttributeGroupReference.class);
    }
    
    /**
     *
     */
    public Collection<LocalAttribute> getLocalAttributes() {
        return getChildren(LocalAttribute.class);
    }
    
    /**
     *
     */
    public Collection<AttributeReference> getAttributeReferences() {
        return getChildren(AttributeReference.class);
    }
    
    /**
     *
     */
    public AnyAttribute getAnyAttribute() {
        List<AnyAttribute> anyAttr = getChildren(AnyAttribute.class);
        return anyAttr.isEmpty() ? null : anyAttr.get(0);
    }
}
