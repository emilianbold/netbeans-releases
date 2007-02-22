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

package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Form;

/**
 * Proxy attribute, acts on behalf of an Attribute.
 * Delegates all calls to the original attribute.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AttributeProxy extends Attribute implements AXIComponentProxy {
   
    /**
     * Creates a new instance of AttributeProxy
     */
    public AttributeProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    private Attribute getShared() {
        return (Attribute)getSharedComponent();
    }
    
    /**
     * Returns the type of this component,
     * may be local, shared, proxy or reference.
     * @see ComponentType.
     */
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
    
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public boolean isReference() {
        return getShared().isReference();
    }
    
    /**
     * Returns the referent if isReference() is true.
     */
    public Attribute getReferent() {
        return getShared().getReferent();
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        return getShared().getName();
    }
    
    /**
     * Sets the name.
     */
    public void setName(String name) {
        getShared().setName(name);
    }    
        
    /**
     * Returns the type. This is expensive, since it uses a visitor
     * to traverse to obtain the type information.
     */    
    public AXIType getType() {
        return getShared().getType();
    }
    
    /**
     * Sets the type.
     */
    public void setType(AXIType datatype) {
        getShared().setType(datatype);
    }
    	
    /**
     * Returns the form.
     */
    public Form getForm() {
        return getShared().getForm();
    }
    
    /**
     * Sets the form.
     */
    public void setForm(Form form) {
        getShared().setForm(form);
    }
    
    /**
     * Returns the fixed value.
     */
    public String getFixed() {
        return getShared().getFixed();
    }
    
    /**
     * Sets the fixed value.
     */
    public void setFixed(String value) {
        getShared().setFixed(value);        
    }
    
    /**
     * Returns the default value.
     */
    public String getDefault() {
        return getShared().getDefault();
    }
    
    /**
     * Sets the default value.
     */
    public void setDefault(String value) {
        getShared().setDefault(value);
    }
    
    /**
     * Returns the use.
     */
    public Use getUse() {
        return getShared().getUse();
    }
    
    /**
     * Sets the use.
     */
    public void setUse(Use value) {
        getShared().setUse(value);
    }    
    
    /**
     * Proxy doesn't get refreshed in the UI. We must notify.
     */
    void forceFireEvent() {
        firePropertyChangeEvent(Attribute.PROP_NAME, null, getName());
    }
}
