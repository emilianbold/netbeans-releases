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
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Represents an Element in XML Schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class Element extends AbstractElement implements AXIType {
    
    /**
     * Creates a new instance of Element
     */
    public Element(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Element
     */
    public Element(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this Element.
     */
    public Element(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
        
    /**
     * Allows a visitor to visit this Element.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public abstract boolean isReference();
            
    /**
     * Returns the referent if isReference() is true.
     */
    public abstract Element getReferent();
    
    /**
     * Returns abstract property.
     */
    public abstract boolean getAbstract();
    
    /**
     * Sets the abstract property.
     */
    public abstract void setAbstract(boolean value);
    
    /**
     * Returns the block.
     */
    public abstract String getBlock();
        
    /**
     * Sets the block property.
     */
    public abstract void setBlock(String value);
    
    /**
     * Returns the final property.
     */
    public abstract String getFinal();
    
    /**
     * Sets the final property.
     */
    public abstract void setFinal(String value);
    
    /**
     * Returns the fixed value.
     */
    public abstract String getFixed();
    
    /**
     * Sets the fixed value.
     */
    public abstract void setFixed(String value);
    
    /**
     * Returns the default value.
     */
    public abstract String getDefault();
    
    /**
     * Sets the default value.
     */
    public abstract void setDefault(String value);
    
    /**
     * Returns the form.
     */
    public abstract Form getForm();
    
    /**
     * Sets the form.
     */
    public abstract void setForm(Form value);
        
    /**
     * Returns the nillable.
     */
    public abstract boolean getNillable();
    
    /**
     * Sets the nillable property.
     */
    public abstract void setNillable(boolean value);

    /**
     * used  by property editor
     */
    public Boolean isNillable() {
        return Boolean.valueOf(getNillable());
    }
	
    /**
     * used  by property editor
     */
    public void setNillable(Boolean nillable) {
        if(nillable != null)
            setNillable(nillable.booleanValue());
    }
	
    /**
     * Returns the complex type of this element, if there is one.
     * Null for element with simple type or anonymous type.
     */
    public abstract AXIType getType();
	
    /**
     * sets the type of this element.
     */
    public abstract void setType(AXIType type);	
        
    /**
     * String representation of this Element.
     */
    public String toString() {
        return getName();
    }
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    protected String finalValue;
    protected String fixedValue;
    protected String defaultValue;
    protected Form form;
    protected String block;
    protected boolean isAbstract;
    protected boolean isNillable;
    
    ////////////////////////////////////////////////////////////////////
    ////////////////// Properties for firing events ////////////////////
    ////////////////////////////////////////////////////////////////////
    public static final String PROP_FINAL         = "final"; // NOI18N
    public static final String PROP_FIXED         = "fixed"; // NOI18N
    public static final String PROP_DEFAULT       = "default"; // NOI18N
    public static final String PROP_FORM          = "form"; // NOI18N
    public static final String PROP_BLOCK         = "block"; // NOI18N
    public static final String PROP_ABSTRACT      = "abstract"; // NOI18N
    public static final String PROP_NILLABLE      = "nillable"; // NOI18N
    public static final String PROP_TYPE          = "type"; // NOI18N	
    public static final String PROP_ELEMENT_REF   = "elementRef"; // NOI18N
}
