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
 * GlobalReferenceProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.basic.editors.GlobalReferenceEditor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This class provides property support for properties having global references.
 * @author Ajit Bhate
 */
public class GlobalReferenceProperty<T extends ReferenceableSchemaComponent>
		extends BaseSchemaProperty {
    
    private Class<T> referenceType;
    private String typeDisplayName;

    /**
     * Creates a new instance of GlobalReferenceProperty.
     * 
     * 
     * @param component The schema component which property belongs to.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @param isPrimitive distinguish between int and Integer. temporary property
     * Assumes that the property editor is default editor for Integer.
     * If special editor needed, subclasses and instances must set it explicitly.
     * @throws java.lang.NoSuchMethodException If no getter and setter for the property are found
     */
    public GlobalReferenceProperty(SchemaComponent component, 
            String property, String dispName, String desc, 
            String typeDisplayName, String referenceTypeDisplayName,
            Class<T> referenceType) 
            throws NoSuchMethodException {
            super(component,NamedComponentReference.class,property,dispName,desc,null);
        this.referenceType = referenceType;
        this.typeDisplayName = typeDisplayName;
    }
    
    /**
     * This api determines if this property supports resetting default value.
     * Overriden to return false always.
     * Subclasses can override if different behaviour expected.
     */
    @Override
    public boolean supportsDefaultValue () {
        return false;
    }

    /**
     * This method returns the property editor.
     * Overridden to return special editor.
     */
    @Override
    public java.beans.PropertyEditor getPropertyEditor() {
        return new GlobalReferenceEditor<T>(super.getComponent(), typeDisplayName, 
                getDisplayName(),referenceType);
    }
    
}
