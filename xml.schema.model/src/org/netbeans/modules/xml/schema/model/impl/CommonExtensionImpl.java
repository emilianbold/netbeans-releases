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
 * CommonExtensionImpl.java
 *
 * Created on October 10, 2005, 11:28 AM
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
import org.netbeans.modules.xml.schema.model.Extension;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public abstract class CommonExtensionImpl extends SchemaComponentImpl implements Extension{
    
    /** Creates a new instance of CommonExtensionImpl */
    public CommonExtensionImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }
    
    public void removeLocalAttribute(LocalAttribute attr) {
        removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }
    
    public void addLocalAttribute(LocalAttribute attr) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(LOCAL_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    public Collection<LocalAttribute> getLocalAttributes() {
        return getChildren(LocalAttribute.class);
    }
    
    public void removeAttributeReference(AttributeReference attr) {
        removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }
    
    public void addAttributeReference(AttributeReference attr) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(LOCAL_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    public Collection<AttributeReference> getAttributeReferences() {
        return getChildren(AttributeReference.class);
    }
    
    public void setBase(GlobalReference<GlobalType> type) {
        setGlobalReference(BASE_PROPERTY, SchemaAttributes.BASE, type);
        //setAttribute(SchemaAttributes.BASE, type.getRawURI());
    }
    
    public GlobalReference<GlobalType> getBase() {
        return resolveGlobalReference(GlobalType.class, SchemaAttributes.BASE);
    }
    
    public void setAnyAttribute(AnyAttribute attr) {
        appendChild(ANY_ATTRIBUTE_PROPERTY, attr);
    }
    
    public AnyAttribute getAnyAttribute() {
        Collection<AnyAttribute> elements = getChildren(AnyAttribute.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        //TODO should we throw exception if there is no definition?
        return null;
    }
    
    public void removeAttributeGroupReference(AttributeGroupReference ref) {
        removeChild(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref);
    }
    
    public void addAttributeGroupReference(AttributeGroupReference ref) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(AnyAttribute.class);
        addBefore(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref, list);
    }
    
    public Collection<AttributeGroupReference> getAttributeGroupReferences() {
        return getChildren(AttributeGroupReference.class);
    }
}
