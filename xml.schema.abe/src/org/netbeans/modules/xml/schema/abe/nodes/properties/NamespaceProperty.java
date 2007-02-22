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
 * NamespaceProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.ui.nodes.schema.SchemaNode;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * This class provides property support for properties having namespace uris.
 * @author Ajit Bhate
 */
public class NamespaceProperty extends BaseABENodeProperty {
    
    private String typeDisplayName;
    /**
     * Creates a new instance of NamespaceProperty.
     * 
     * 
     * @param component The schema component which property belongs to.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @throws java.lang.NoSuchMethodException If no getter and setter for the property are found
     */
    public NamespaceProperty(AXIComponent component,
            String property, String dispName, String desc, String typeDisplayName) 
            throws NoSuchMethodException {
            super(component,String.class,property,dispName,desc,null);
            this.typeDisplayName = typeDisplayName;
    }
    
    public void setValue(Object o) throws IllegalAccessException, InvocationTargetException {
        if(o==null) {
            super.setValue(null);
        } else if(o instanceof String) {
            try {
                new URI((String) o);
                super.setValue(o);
            } catch (URISyntaxException urse) {
                String msg = NbBundle.getMessage(SchemaNode.class, "MSG_Invalid_URI",o); //NOI18N
                IllegalArgumentException iae = new IllegalArgumentException(msg);
                ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                        msg, msg, urse, new java.util.Date());
                throw iae;
            }
        }
    }
    /**
     * This method returns the property editor.
     * Overridden to return special editor.
     */
    @Override
    public java.beans.PropertyEditor getPropertyEditor() {
        return new NamespaceEditor((AXIComponent) super.getComponent(), typeDisplayName, 
                getDisplayName());
    }
    
}
