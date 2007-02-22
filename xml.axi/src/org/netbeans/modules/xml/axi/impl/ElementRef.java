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
 * ElementRef.java
 *
 * Created on May 5, 2006, 11:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.axi.impl;

import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Represents an Element reference. For an Element reference
 * most of the calls are delegated to the original Element,
 * except for calls related to min and max occurs.
 *
 * See http://www.w3.org/TR/xmlschema-1/#d0e4233.
 * @author Samaresh (Samaresh.Panda@Sun.Com)
*/
public class ElementRef extends Element {
                
    /**
     * Creates a new instance of ElementRef
     */
    public ElementRef(AXIModel model, Element referent) {
        super(model, referent);
    }
    
    /**
     * Creates a new instance of ElementRef
     */
    public ElementRef(AXIModel model, SchemaComponent component, Element referent) {
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
    public Element getReferent() {
        return (Element)getSharedComponent();
    }
        
    /**
     * Sets the new referent.
     */
    public void setRef(Element referent) {
        ElementImpl oldRef = (ElementImpl) getReferent();
        if(oldRef == referent)
            return;        
        oldRef.removeListener(this);
        setSharedComponent(referent);
        firePropertyChangeEvent(PROP_ELEMENT_REF, oldRef, referent);
        forceFireEvent();
        if(canVisitChildren()) {
            removeAllChildren();
            Util.addProxyChildren(this, referent, null);
        }
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
        getReferent().setName(name);
    }
    
    /**
     * Returns abstract property.
     */
    public boolean getAbstract() {
        return getReferent().getAbstract();
    }
    
    /**
     * Sets the abstract property.
     */
    public void setAbstract(boolean value) {
        getReferent().setAbstract(value);
    }
    
    /**
     * Returns the block.
     */
    public String getBlock() {
        return getReferent().getBlock();
    }
        
    /**
     * Sets the block property.
     */
    public void setBlock(String value) {
        getReferent().setBlock(value);
    }
    
    /**
     * Returns the final property.
     */
    public String getFinal() {
        return getReferent().getFinal();
    }
    
    /**
     * Sets the final property.
     */
    public void setFinal(String value) {
        getReferent().setFinal(value);
    }
    
    /**
     * Returns the fixed value.
     */
    public String getFixed() {
        return getReferent().getFixed();
    }
    
    /**
     * Sets the fixed value.
     */
    public void setFixed(String value) {
        getReferent().setFixed(value);
    }
    
    /**
     * Returns the default value.
     */
    public String getDefault() {
        return getReferent().getDefault();
    }
    
    /**
     * Sets the default value.
     */
    public void setDefault(String value) {
        getReferent().setDefault(value);
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
    public void setForm(Form value) {
        getReferent().setForm(value);
    }
        
    /**
     * Returns the nillable.
     */
    public boolean getNillable() {
        return getReferent().getNillable();
    }
    
    /**
     * Sets the nillable property.
     */
    public void setNillable(boolean value) {
        getReferent().setNillable(value);
    }
    
    /**
     * Adds a Compositor as its child.
     * Compositor must always be at the 0th index.
     */
    public void addCompositor(Compositor compositor) {
        getReferent().addCompositor(compositor);
    }
    
    /**
     * Removes a Compositor.
     */
    public void removeCompositor(Compositor compositor) {
        getReferent().removeCompositor(compositor);
    }
    
    /**
     * Adds an Element as its child.
     * If attributes exist, add the new child before all attributes.
     * Attributes must always be added at the end of the list.
     */
    public void addElement(AbstractElement child) {
        getReferent().addElement(child);
    }
    
    /**
     * Removes an Element.
     */
    public void removeElement(AbstractElement element) {
        getReferent().removeElement(element);
    }
    
    /**
     * Adds an attribute.
     */
    public void addAttribute(AbstractAttribute attribute) {
        getReferent().addAttribute(attribute);
    }
    
    /**
     * Removes an attribute.
     */
    public void removeAttribute(AbstractAttribute attribute) {
        getReferent().removeAttribute(attribute);
    }
	
    public AXIType getType() {
        return getReferent().getType();
    }
    
    /**
     * sets the type of this element.
     */
    public void setType(AXIType type) {
        if(type instanceof Element) {
            setRef((Element)type);
            return;
        }
        
        int index = this.getIndex();
        AXIComponent parent = getParent();
        Element e = getModel().getComponentFactory().createElement();
        e.setName(getReferent().getName());
        parent.removeChild(this);
        parent.insertAtIndex(Element.PROP_ELEMENT, e, index);
        e.setType(type);
    }
	
    /**
     * Returns the compositor.
     */
    public Compositor getCompositor() {
        return getReferent().getCompositor();
    }
    
    /**
     * For an element-ref or attribute-ref, most of the properties come from the actual
     * element or attribute. So when something changes in the ref, we must forcibly fire
     * an event so that the UI updates itself.
     */
    void forceFireEvent() {
        firePropertyChangeEvent(Element.PROP_NAME, null, getReferent().getName());
    }
}
