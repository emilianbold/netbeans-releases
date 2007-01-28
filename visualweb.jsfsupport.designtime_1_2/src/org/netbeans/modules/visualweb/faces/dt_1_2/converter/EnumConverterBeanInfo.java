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
package org.netbeans.modules.visualweb.faces.dt_1_2.converter;

import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import java.beans.*;
import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.convert.EnumConverter;

public class EnumConverterBeanInfo extends HtmlNonGeneratedBeanInfoBase {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(EnumConverterBeanInfo.class);

    /**
     * Construct a <code>EnumConverterBeanInfo</code> instance
     */
    public EnumConverterBeanInfo() {
        beanClass = EnumConverter.class;
        iconFileName_C16 = "EnumConverter_C16"; //NOI18N
        iconFileName_C32 = "EnumConverter_C32"; //NOI18N
        iconFileName_M16 = "EnumConverter_M16"; //NOI18N
        iconFileName_M32 = "EnumConverter_M32"; //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "enumConverter"); //NOI18N
            beanDescriptor.setDisplayName(bundle.getMessage("enumConvert")); //NOI18N
            beanDescriptor.setShortDescription(bundle.getMessage("enumConvertShortDesc")); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_enum_converter");
        }
        return beanDescriptor;
    }


    private PropertyDescriptor[] propDescriptors;

    /**
     * Returns the PropertyDescriptor array which describes
     * the property meta-data for this JavaBean
     *
     * @return An array of PropertyDescriptor objects
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propDescriptors == null) {
            try {
                PropertyDescriptor _transient = new PropertyDescriptor("transient", beanClass, "isTransient", "setTransient"); //NOI18N

                propDescriptors = new PropertyDescriptor[] {
                    _transient
                };
            } catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }

        return propDescriptors;
    }
}
