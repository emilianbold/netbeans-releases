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

import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.datatype.StringType;
import org.netbeans.modules.xml.axi.impl.DatatypeBuilder;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Attribute.Use;

/**
 * Represents an attribute in XML Schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class Attribute extends AbstractAttribute implements AXIType {
	
    /**
     * Creates a new instance of Attribute
     */
    public Attribute(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Attribute
     */
    public Attribute(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }

    /**
     * Creates a proxy for this Attribute.
     */
    public Attribute(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Attribute.
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
    public abstract Attribute getReferent();
    
    /**
     * Sets the name.
     */
    public abstract void setName(String name);
        
    /**
     * Returns the type. This is expensive, since it uses a visitor
     * to traverse to obtain the type information.
     */    
    public abstract AXIType getType();
    
    /**
     * Sets the type.
     */
    public abstract void setType(AXIType type);
    	
    /**
     * Returns the form.
     */
    public abstract Form getForm();
    
    /**
     * Sets the form.
     */
    public abstract void setForm(Form form);
    
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
     * Returns the use.
     */
    public abstract Use getUse();
    
    /**
     * Sets the use.
     */
    public abstract void setUse(Use use);
        
    /**
     * Returns the string representation of this Attribute.
     */
    public String toString() {        
        return getName();              //NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    protected String name;
    protected Form form;
    protected Use use;
    protected String defaultValue;
    protected String fixedValue;
    protected AXIType datatype;

    ////////////////////////////////////////////////////////////////////
    ////////////////// Properties for firing events ////////////////////
    ////////////////////////////////////////////////////////////////////
    public static final String PROP_NAME            = "name"; //NOI18N
    public static final String PROP_FORM            = "form"; //NOI18N
    public static final String PROP_USE             = "use"; //NOI18N
    public static final String PROP_DEFAULT         = "default"; //NOI18N
    public static final String PROP_FIXED           = "fixed"; //NOI18N
    public static final String PROP_TYPE            = "type"; //NOI18N    
    public static final String PROP_ATTRIBUTE_REF   = "attributeRef"; // NOI18N
}
