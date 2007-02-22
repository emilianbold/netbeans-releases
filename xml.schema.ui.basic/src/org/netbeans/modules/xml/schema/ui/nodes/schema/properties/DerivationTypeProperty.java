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
 * DerivationTypeProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.basic.editors.DerivationTypeEditor;

/**
 *
 * @author Ajit Bhate
 */
public class DerivationTypeProperty extends BaseSchemaProperty {
    
    private String parentDisplayName;
    
    /** Creates a new instance of DerivationTypeProperty */
    public DerivationTypeProperty(SchemaComponent component, String property,
            String propName, String propDesc, String parentDisplayName)
            throws NoSuchMethodException {
        super(component, Set.class, property, propName, propDesc, null);
        this.parentDisplayName = parentDisplayName;
    }
    
    public PropertyEditor getPropertyEditor() {
        return new DerivationTypeEditor(getComponent(),
                super.getName(),
                parentDisplayName);
    }
    
    public Object getValue() throws IllegalAccessException,
            IllegalAccessException,	InvocationTargetException {
        try {
            return super.getValue();
        } catch (InvocationTargetException ite) {
            if(ite.getCause() instanceof IllegalArgumentException) {
                return NbBundle.getMessage(DerivationTypeProperty.class,
                        "LBL_Invalid_DerivationType_Value",
                        getComponent().getAnyAttribute(
                        new javax.xml.namespace.QName("",getName())));
            }
            throw ite;
        }
    }
}
