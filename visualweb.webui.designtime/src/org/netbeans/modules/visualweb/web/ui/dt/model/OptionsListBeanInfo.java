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
package org.netbeans.modules.visualweb.web.ui.dt.model;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.OptionsPropertyEditor;
import org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.SelectedValuesPropertyEditor;
import com.sun.rave.web.ui.model.OptionsList;

/**
 * BeanInfo for {@link org.netbeans.modules.visualweb.web.ui.model.OptionsList}.
 *
 * @author gjmurphy
 */
public abstract class OptionsListBeanInfo extends SimpleBeanInfo {

    BeanDescriptor beanDescriptor;

    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(this.getBeanClass());
        }
        return beanDescriptor;
    }

    private PropertyDescriptor[] propertyDescriptors;

    public PropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors != null)
            return propertyDescriptors;
        try {
            PropertyDescriptor[] additionalPropertyDescriptors =
                    getAdditionalPropertyDescriptors();
            PropertyDescriptor optionsDesc = new PropertyDescriptor( "options",
                    this.getBeanClass(), "getOptions", "setOptions");
            optionsDesc.setPropertyEditorClass(OptionsPropertyEditor.class);
            PropertyDescriptor selectedValueDesc = new PropertyDescriptor( "selectedValue",
                    this.getBeanClass(), "getSelectedValue", "setSelectedValue");
            selectedValueDesc.setPropertyEditorClass(SelectedValuesPropertyEditor.class);
            propertyDescriptors = new PropertyDescriptor[additionalPropertyDescriptors.length + 2];
            int i = 0;
            while (i < additionalPropertyDescriptors.length) {
                propertyDescriptors[i] = additionalPropertyDescriptors[i];
                i++;
            }
            propertyDescriptors[i++] = optionsDesc;
            propertyDescriptors[i++] = selectedValueDesc;
        } catch(IntrospectionException e) {
            propertyDescriptors = new PropertyDescriptor[0];
        }
        return propertyDescriptors;
    }

    abstract protected PropertyDescriptor[] getAdditionalPropertyDescriptors()
    throws IntrospectionException;

    abstract protected Class getBeanClass();

}
