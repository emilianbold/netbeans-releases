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
package org.netbeans.modules.xml.axi;

import java.util.List;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Abstract element.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractElement extends AXIContainer {
    
    /**
     * Creates a new instance of Element
     */
    public AbstractElement(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Element
     */
    public AbstractElement(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this Element.
     */
    public AbstractElement(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Element.
     */
    public abstract void accept(AXIVisitor visitor);
            
    /**
     * Returns the MinOccurs.
     */
    public String getMinOccurs() {
        return minOccurs;
    }
    
    /**
     * Sets the MinOccurs.
     */
    public void setMinOccurs(String value) {        
        String oldValue = getMinOccurs();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.minOccurs = value;
        firePropertyChangeEvent(PROP_MINOCCURS, oldValue, value);
    }
	
    /**
     * Returns the MaxOccurs.
     */
    public String getMaxOccurs() {
        return maxOccurs;
    }
    
    /**
     * Sets the MaxOccurs.
     */
    public void setMaxOccurs(String value) {        
        String oldValue = getMaxOccurs();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.maxOccurs = value;
        firePropertyChangeEvent(PROP_MAXOCCURS, oldValue, value);
    }
	
    /**
     * true if #getMaxOccurs() and #getMinOccurs() allow multiciplity outside
     * [0,1], false otherwise. This method is only accurate after the element
     * has been inserted into the model.
     */
    public boolean allowsFullMultiplicity() {
		return !(getParent() instanceof Compositor && 
				((Compositor)getParent()).getType() == Compositor.CompositorType.ALL);
    }	
    
    protected String minOccurs = "1";
    protected String maxOccurs = "1";
    
    public static final String PROP_MINOCCURS     = "minOccurs"; // NOI18N
    public static final String PROP_MAXOCCURS     = "maxOccurs"; // NOI18N
    public static final String PROP_ELEMENT       = "element"; // NOI18N
}
