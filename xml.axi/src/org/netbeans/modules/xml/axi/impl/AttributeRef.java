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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AttributeRef.java
 *
 * Created on May 5, 2006, 12:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Form;

/**
 * Represents an Attribute reference. For an Attribute reference
 * name, type and form must be absent, that is, calls on name, type
 * and form must be delegated to the original.
 *
 * See http://www.w3.org/TR/xmlschema-1/#d0e2403.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AttributeRef extends Attribute {
   
    /**
     * Creates a new instance of AttributeRef
     */
    public AttributeRef(AXIModel model, Attribute referent) {
        super(model, referent);
    }
    
    /**
     * Creates a new instance of AttributeRef
     */
    public AttributeRef(AXIModel model, SchemaComponent component, Attribute referent) {
        super(model, component);
        super.setSharedComponent(referent);
    }
    
    /**
     * Returns the type of this component,
     * may be local, shared, proxy or reference.
     * @see ComponentType.
     */
    public ComponentType getComponentType() {
        return ComponentType.REFERENCE;
    }
    
    /**
     * Returns the referent if isReference() is true.
     */
    public Attribute getReferent() {
        return (Attribute)getSharedComponent();
    }
        
    /**
     * Sets the new referent.
     */
    public void setRef(Attribute referent) {
        AttributeImpl oldRef = (AttributeImpl) getReferent();
        if(oldRef == referent)
            return;
        oldRef.removeListener(this);
        super.setSharedComponent(referent);
        firePropertyChangeEvent(PROP_ATTRIBUTE_REF, oldRef, referent);
        forceFireEvent();
    }
    
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public boolean isReference() {
        return true;
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        return getReferent().getName();
    }
    
    /**
     * Sets the name.
     */
    public void setName(String name) {
        for(Attribute a : getModel().getRoot().getAttributes()) {
            if(a.getName().equals(name)) {
                setRef(a);
                return;
            }
        }
        getReferent().setName(name);
    }    
        
    /**
     * Returns the type. This is expensive, since it uses a visitor
     * to traverse to obtain the type information.
     */    
    public AXIType getType() {
        return getReferent().getType();
    }
    
    /**
     * Sets the type.
     */
    public void setType(AXIType type) {
        if(type instanceof Attribute) {
            setRef((Attribute)type);
            return;
        }
        
        int index = this.getIndex();
        AXIComponent parent = getParent();
        Attribute a = getModel().getComponentFactory().createAttribute();
        a.setName(getReferent().getName());
        parent.removeChild(this);
        parent.insertAtIndex(Attribute.PROP_ATTRIBUTE, a, index);
        a.setType(type);
    }
    	
    /**
     * Returns the form.
     */
    public Form getForm() {
        return getReferent().getForm();
    }
    
    /**
     * Sets the form.
     */
    public void setForm(Form form) {
        getReferent().setForm(form);
    }
    
    /**
     * Returns the fixed value.
     */
    public String getFixed() {
        return fixedValue;
    }
    
    /**
     * Sets the fixed value.
     */
    public void setFixed(String value) {        
        String oldValue = getFixed();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.fixedValue = value;
        firePropertyChangeEvent(PROP_FIXED, oldValue, value);
    }
    
    /**
     * Returns the default value.
     */
    public String getDefault() {
        return defaultValue;
    }
    
    /**
     * Sets the default value.
     */
    public void setDefault(String value) {
        String oldValue = getDefault();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.defaultValue = value;
        firePropertyChangeEvent(PROP_DEFAULT, oldValue, value);
    }
    
    /**
     * Returns the use.
     */
    public Use getUse() {
        return use;
    }
    
    /**
     * Sets the use.
     */
    public void setUse(Use value) {
        Use oldValue = getUse();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.use = value;
        firePropertyChangeEvent(PROP_USE, oldValue, value);
    }
    
    /**
     * For an element-ref or attribute-ref, most of the properties come from the actual
     * element or attribute. So when something changes in the ref, we must forcibly fire
     * an event so that the UI updates itself.
     */
    void forceFireEvent() {
        firePropertyChangeEvent(Attribute.PROP_NAME, null, getReferent().getName());
    }
    
}
