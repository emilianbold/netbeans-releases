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
 * BooleanProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.basic.editors.BooleanDefaultFalseEditor;
import org.netbeans.modules.xml.schema.ui.basic.editors.BooleanEditor;

/**
 * This class provides property support for properties having boolean values.
 * It provides support for properties which default to null or to false
 * when not specified.
 * @author Ajit Bhate
 */
public class BooleanProperty extends BaseSchemaProperty {
    
    /**
     * Creates a new instance of BooleanProperty.
     * @param component The schema component which property belongs to.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @param isDefaultFalse If the default value for this property is false.
     *     In such case BooleanDefaultFalseEditor will be used as propertyeditor, 
     *     BooleanEditor otherwise which supports null as default values.
     * @throws java.lang.NoSuchMethodException If no getter and setter for the property are found
     */
    public BooleanProperty(SchemaComponent component, String property,
            String dispName, String desc, boolean isDefaultFalse)
            throws NoSuchMethodException {
        super(component,
                // Somehow netbeans does not support special property editor
                // if Boolean.class is used, so use Object as valueType and 
                // provide getter setter methods
                Object.class,
                // The getter method for schema properties start with is
                component.getClass().getMethod(BaseSchemaProperty.
                firstLetterToUpperCase(property, "is"), new Class[0]),
                component.getClass().getMethod(BaseSchemaProperty.
                firstLetterToUpperCase(property, "set"), new Class[]{Boolean.class}),
                property,
                dispName,
                desc,
                isDefaultFalse ?
                    BooleanDefaultFalseEditor.class :
                    BooleanEditor.class
                );
    }
    
}
