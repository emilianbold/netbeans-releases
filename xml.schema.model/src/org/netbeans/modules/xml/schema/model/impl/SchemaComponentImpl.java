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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.XMLConstants;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Attribute;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.ModelAccess;
import org.netbeans.modules.xml.xam.Referenceable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author rico
 * @author Vidhya Narayanan
 */
public abstract class SchemaComponentImpl
        extends AbstractComponent<SchemaComponent, SchemaModel>
        implements SchemaComponent, ModelAccess.NodeUpdater {
    
    public SchemaComponentImpl(SchemaModel model, Element e) {
        super(model, e);
    }
    
    public abstract Class<? extends SchemaComponent> getComponentType();
    
    protected String getNamespaceURI() {
        return XMLConstants.W3C_XML_SCHEMA_NS_URI;
    }
    
    /**
     * Leave this method as abstract
     */
    public abstract void accept(SchemaVisitor v);
    
    protected static Element createNewComponent(SchemaElements type, SchemaModelImpl model) {
        return model.getDocument().createElementNS(
                XMLConstants.W3C_XML_SCHEMA_NS_URI,
                type.getName());
    }
    
    protected void populateChildren(List<SchemaComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    SchemaComponent comp = (SchemaComponent)getModel().getFactory().create((Element)n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    
    /**
     * @return true if the elements are from the same schema model.
     */
    public final boolean fromSameModel(SchemaComponent other) {
        return getSchemaModel().equals(other.getSchemaModel());
    }
    
    /**
     * @return the schema model where this element is being used or null if not
     * currently part of a model.
     */
    public final SchemaModel getSchemaModel() {
        return super.getModel();
    }
    
    /**
     * Annotation always gets added as the first child.
     */
    public void setAnnotation(Annotation annotation) {
        List<Class<? extends SchemaComponent>> types = new ArrayList<Class<? extends SchemaComponent>>(1);
        types.add(SchemaComponent.class);
        setChildBefore(Annotation.class, ANNOTATION_PROPERTY, annotation, types);
    }
    
    public Annotation getAnnotation() {
        List<Annotation> annotations = getChildren(Annotation.class);
        return annotations.isEmpty() ? null : annotations.iterator().next();
    }
    
    /**
     * Returns type of the given attribute.
     * The type should either be:
     * 1. String or wrappers for primitive types (Boolean, Integer,...)
     * 2. An enum with toString() overridden to return string value by XSD specs.
     * 3. java.util.Set
     *
     * @param attribute the attribute enum name
     */
    protected Class getAttributeType(Attribute attribute) {
        return attribute.getType();
    }
    
    /**
     * Returns type of member in cases attribute type is collections.
     */
    protected Class getAttributeMemberType(Attribute attribute) {
        return attribute.getMemberType();
    }
    
    protected Object getAttributeValueOf(Attribute attr, String s) {
        if (s == null) {
            return null;
        }
        Class c = getAttributeType(attr);
        if (String.class.isAssignableFrom(c)) {
            return s;
        } else if (Boolean.class.isAssignableFrom(c)) {
            return Boolean.valueOf(s);
        } else if (Integer.class.isAssignableFrom(c)) {
            return Integer.valueOf(s);
        } else if (Enum.class.isAssignableFrom(c)) {
            Class<Enum> enumClass = (Class<Enum>) c;
            return Util.parse(enumClass, s);
        } else if (Set.class.isAssignableFrom(c)) {
            return Util.valuesOf(getAttributeMemberType(attr), s);
        }
        
        assert(false); // should never reached within this model implementation
        return null;
    }
    
    protected <T extends ReferenceableSchemaComponent> GlobalReferenceImpl<T> resolveGlobalReference(Class<T>c, SchemaAttributes attrName){
        String v = getAttribute(attrName);
        return v == null ? null : new GlobalReferenceImpl<T>(c, this, v);
    }
    
    protected <T extends Referenceable<SchemaComponent>> void setGlobalReference(String propertyName, SchemaAttributes attrName, GlobalReference<T> t){
        String attrValue = null;
        if (t != null) {
            attrValue = getPrefixedName(t.getEffectiveNamespace(), t.get().getName());
        }
        setAttribute(propertyName, attrName, attrValue);
    }
    
    protected String getPrefixedName(String namespace, String localName) {
        String prefix = ensurePrefixedNamespace(namespace);
        return prefix.length() == 0 ? localName:prefix+':'+localName; //NOI18N
    }
    
    /**
     * This method will ensure the given namespace is prefixed at the root level
     * if necessary and return an appropriate prefixed name.
     */
    private String ensurePrefixedNamespace(String namespace) {
        String prefix = lookupPrefix(namespace);
        if (prefix == null) { // backing node might be not added to xdm tree yet
            prefix = getModel().getSchema().getPeer().lookupPrefix(namespace);
        }
        return prefix == null ? "" : prefix;
    }
    
    protected Element checkNodeRef() {
        Element e = (Element)getPeer();
        if (e == null) {
            throw new IllegalArgumentException("Valid Node reference must exist"); // NOI18N
        }
        return e;
    }
	
    public <T extends ReferenceableSchemaComponent> GlobalReference<T> 
            createReferenceTo(T referenced, Class<T> type) {
        return getModel().getFactory().createGlobalReference(referenced, type, this);
    }

    public void setId(String id) {
        setAttribute(ID_PROPERTY, SchemaAttributes.ID, id);
    }

    public String getId() {
        return getAttribute(SchemaAttributes.ID);
    }
    
    protected String getAttributeValue(SchemaAttributes attr) {
        return getAttribute(attr);
    }
}

