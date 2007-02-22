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

import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Any.ProcessContents;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Represents anyAttribute in XML Schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AnyAttribute extends AbstractAttribute {
    
    /**
     * Creates a new instance of AnyAttribute
     */
    public AnyAttribute(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of AnyAttribute
     */
    public AnyAttribute(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a new instance of AnyAttributeProxy
     */
    public AnyAttribute(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }    
    
    /**
     * Allows a visitor to visit this Attribute.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        return "anyAttribute"; //NOI18N
    }    

    /**
     * Returns the processContents.
     */
    public ProcessContents getProcessContents() {
        return processContents;
    }
    
    /**
     * Sets the processContents.
     */
    public void setProcessContents(ProcessContents value) {
        ProcessContents oldValue = getProcessContents();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue == value) ) {
            return;
        }
        this.processContents = value;
        firePropertyChangeEvent(PROP_PROCESSCONTENTS, oldValue, value);
    }
    
    /**
     * Returns the target namespace.
     */
    public String getTargetNamespace() {
        return namespace;
    }

    /**
     * Sets the target namespace.
     */
    public void setTargetNamespace(String value) {
        String oldValue = getTargetNamespace();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.namespace = value;
        firePropertyChangeEvent(PROP_NAMESPACE, oldValue, value);
    }
    
    /**
     * String representation of this Element.
     */
    public String toString() {        
        return getName();
    }	
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    private String namespace;
    private ProcessContents processContents;
    
    ////////////////////////////////////////////////////////////////////
    ////////////////// Properties for firing events ////////////////////
    ////////////////////////////////////////////////////////////////////
    public static final String PROP_NAMESPACE         = "namespace"; // NOI18N
    public static final String PROP_PROCESSCONTENTS   = "processContents"; // NOI18N
}
