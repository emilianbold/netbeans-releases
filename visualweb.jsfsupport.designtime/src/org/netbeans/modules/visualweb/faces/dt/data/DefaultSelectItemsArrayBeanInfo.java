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
package org.netbeans.modules.visualweb.faces.dt.data;

import com.sun.rave.faces.data.DefaultSelectItemsArray;
import com.sun.rave.propertyeditors.StringArrayPropertyEditor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * BeanInfo for {@link DefaultSelectItemsArray}.
 *
 * @author gjmurphy
 */
public class DefaultSelectItemsArrayBeanInfo extends SimpleBeanInfo {

    private PropertyDescriptor[] propertyDescriptors;

    public PropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors != null)
            return propertyDescriptors;
        try {
            PropertyDescriptor itemsDesc = new PropertyDescriptor( "items",
                    DefaultSelectItemsArray.class, "getItems", "setItems");
            itemsDesc.setPropertyEditorClass(StringArrayPropertyEditor.class);
            propertyDescriptors = new PropertyDescriptor[] { itemsDesc };
        } catch(IntrospectionException e) {
            propertyDescriptors = new PropertyDescriptor[0];
        }
        return propertyDescriptors;
    }

}
