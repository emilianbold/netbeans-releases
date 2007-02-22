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
 * FormProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import java.lang.reflect.InvocationTargetException;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.basic.editors.FormPropertyEditor;

/**
 *
 * @author Ajit Bhate
 */
public class FormProperty extends BaseSchemaProperty {
    
    /** Creates a new instance of FormProperty */
    public FormProperty(SchemaComponent component, String property,
            String propName, String propDesc)
            throws NoSuchMethodException {
        super(component, Form.class, property, propName, propDesc, FormPropertyEditor.class);
    }
    
    public Object getValue() throws IllegalAccessException,
            IllegalAccessException,	InvocationTargetException {
        try {
            return super.getValue();
        } catch (InvocationTargetException ite) {
            if(ite.getCause() instanceof IllegalArgumentException) {
                return null;
            }
            throw ite;
        }
    }
}
