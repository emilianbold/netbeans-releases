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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.w3c.dom.Element;


/**
 *
 * @author rico
 */
public abstract class CommonComplexTypeImpl extends SchemaComponentImpl implements ComplexType{
    
    /** Creates a new instance of CommonComplexTypeImpl */
    public CommonComplexTypeImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }
    
    public void setMixed(Boolean mixed) {
        setAttribute(MIXED_PROPERTY , SchemaAttributes.MIXED, mixed);
    }
    
    public Boolean isMixed() {
        String s = getAttribute(SchemaAttributes.MIXED);
        return s == null ? null : Boolean.valueOf(s);
    }
    
    public void addLocalAttribute(LocalAttribute attr) {
         //If group, sequence, choice, or all is the ComplexTypeDefinition,
        //the elements must appear in the following order:
        //1. group | sequence | choice | all
        //2. attribute | attributeGroup
        //3. anyAttribute
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(Annotation.class); 
        list.add(ComplexTypeDefinition.class);
        addAfter(LOCAL_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    public Collection<LocalAttribute> getLocalAttributes() {
        return getChildren(LocalAttribute.class);
    }
    
    public void removeLocalAttribute(LocalAttribute attr) {
        removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }
    
    public void addAttributeReference(AttributeReference attr) {
         //If group, sequence, choice, or all is the ComplexTypeDefinition,
        //the elements must appear in the following order:
        //1. group | sequence | choice | all
        //2. attribute | attributeGroup
        //3. anyAttribute
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(Annotation.class); 
        list.add(ComplexTypeDefinition.class);
        addAfter(LOCAL_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    public Collection<AttributeReference> getAttributeReferences() {
        return getChildren(AttributeReference.class);
    }
    
    public void removeAttributeReference(AttributeReference attr) {
        removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }
    
    public void setAnyAttribute(AnyAttribute attr) {
        //If group, sequence, choice, or all is the ComplexTypeDefinition,
        //the elements must appear in the following order:
        //1. group | sequence | choice | all
        //2. attribute | attributeGroup
        //3. anyAttribute
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(Annotation.class); 
        list.add(ComplexTypeDefinition.class);
        list.add(AttributeGroupReference.class);
        
        setChild(AnyAttribute.class, ANY_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    public AnyAttribute getAnyAttribute() {
        Collection<AnyAttribute> elements = getChildren(AnyAttribute.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    public void addAttributeGroupReference(AttributeGroupReference ref) {
        //If group, sequence, choice, or all is the ComplexTypeDefinition,
        //the elements must appear in the following order:
        //1. group | sequence | choice | all
        //2. attribute | attributeGroup
        //3. anyAttribute
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(Annotation.class); 
        list.add(ComplexTypeDefinition.class);
        addAfter(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref, list);
    }
    
    public void removeAttributeGroupReference(AttributeGroupReference ref) {
        removeChild(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref);
    }
    
    public Collection<AttributeGroupReference> getAttributeGroupReferences() {
        return getChildren(AttributeGroupReference.class);
    }
    
    public void setDefinition(ComplexTypeDefinition content) {
        //If group, sequence, choice, or all is the ComplexTypeDefinition,
        //the elements must appear in the following order:
        //1. group | sequence | choice | all
        //2. attribute | attributeGroup
        //3. anyAttribute
        Collection<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        setChild(ComplexTypeDefinition.class, DEFINITION_PROPERTY, content, list);
    }
    
    public ComplexTypeDefinition getDefinition() {
        Collection<ComplexTypeDefinition> elements = getChildren(ComplexTypeDefinition.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        //TODO should we throw exception if there is no definition?
        return null;
    }
    
    public boolean getMixedEffective() {
        Boolean v = isMixed();
        return v == null ? getMixedDefault() : v;
    }

    public boolean getMixedDefault() {
        return false;
    }
}
