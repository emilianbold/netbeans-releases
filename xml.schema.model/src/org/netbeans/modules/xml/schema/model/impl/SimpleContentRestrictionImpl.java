/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * SimpleContentRestrictionImpl.java
 *
 * Created on October 6, 2005, 9:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class SimpleContentRestrictionImpl extends CommonSimpleRestrictionImpl implements SimpleContentRestriction{
    
    /** Creates a new instance of SimpleContentRestrictionImpl */
    protected SimpleContentRestrictionImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.RESTRICTION, model));
    }
    
    public SimpleContentRestrictionImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return SimpleContentRestriction.class;
	}
    
    public void removeLocalAttribute(LocalAttribute attr) {
        removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }
    
    public void addLocalAttribute(LocalAttribute attr) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(LOCAL_ATTRIBUTE_PROPERTY, (SchemaComponentImpl)attr, list);
    }
    
    public Collection<LocalAttribute> getLocalAttributes() {
        return getChildren(LocalAttribute.class);
    }
    
    public void removeAttributeReference(AttributeReference attr) {
        removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }
    
    public void addAttributeReference(AttributeReference attr) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(LOCAL_ATTRIBUTE_PROPERTY, (SchemaComponentImpl)attr, list);
    }
    
    public Collection<AttributeReference> getAttributeReferences() {
        return getChildren(AttributeReference.class);
    }
    
    public void removeAttributeGroupReference(AttributeGroupReference ref) {
        removeChild(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref);
    }
    
    public void addAttributeGroupReference(AttributeGroupReference ref) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref, list);
    }
    
    public Collection<AttributeGroupReference> getAttributeGroupReferences() {
        return getChildren(AttributeGroupReference.class);
    }
    
    public void setAnyAttribute(AnyAttribute attr) {
        //anyAttribute should always be last
        appendChild(ANY_ATTRIBUTE_PROPERTY, attr);
    }
    
    public AnyAttribute getAnyAttribute() {
        Collection<AnyAttribute> elements = getChildren(AnyAttribute.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
}
