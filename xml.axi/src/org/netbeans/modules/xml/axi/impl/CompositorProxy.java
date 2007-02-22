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
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.Element;

/**
 * Proxy compositor, acts on behalf of an original Compositor.
 * Delegates all calls to the original Compositor.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompositorProxy extends Compositor implements AXIComponentProxy {
            
    
    /**
     * Creates a new instance of CompositorProxy
     */
    public CompositorProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
            
    private Compositor getShared() {
        return (Compositor)getSharedComponent();
    }
    
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
    
    /**
     * Returns the type of this content model.
     */
    public CompositorType getType() {
        return getShared().getType();
    }
    
    /**
     * Returns the type of this content model.
     */
    public void setType(CompositorType value) {
        getShared().setType(value);
    }
    
    /**
     * Returns the min occurs.
     */
    public String getMinOccurs() {
        return getShared().getMinOccurs();            
    }
    
    public void setMinOccurs(String value) {
        getShared().setMinOccurs(value);
    }
    
    /**
     * Returns the max occurs.
     */
    public String getMaxOccurs() {
        return getShared().getMaxOccurs();
    }
    
    public void setMaxOccurs(String value) {
        getShared().setMaxOccurs(value);
    }
        
    /**
     * Adds a Compositor as its child.
     */
    public void addCompositor(CompositorProxy compositor) {
        getShared().addCompositor(compositor);
    }
    
    /**
     * Removes an Compositor.
     */
    public void removeCompositor(CompositorProxy compositor) {
        getShared().removeCompositor(compositor);
    }
    
    /**
     * Adds an Element as its child.
     */
    public void addElement(Element element) {
        getShared().addElement(element);
    }
        
    /**
     * Removes an Element.
     */
    public void removeElement(Element element) {
        getShared().removeElement(element);
    }
    
    public String toString() {        
        return getShared().toString();
    }
    
}
