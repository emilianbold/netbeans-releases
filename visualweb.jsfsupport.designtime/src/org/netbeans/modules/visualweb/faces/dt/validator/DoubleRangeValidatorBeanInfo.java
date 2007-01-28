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
package org.netbeans.modules.visualweb.faces.dt.validator;

import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import java.beans.*;
import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.propertyeditors.DoublePropertyEditor;
import javax.faces.validator.DoubleRangeValidator;

public class DoubleRangeValidatorBeanInfo extends HtmlNonGeneratedBeanInfoBase {  // SimpleBeanInfo

    private static final ComponentBundle bundle = ComponentBundle.getBundle(DoubleRangeValidatorBeanInfo.class);

    /**
     * Construct a <code>DoubleRangeValidatorBeanInfo</code> instance
     */
    public DoubleRangeValidatorBeanInfo() {
        beanClass = DoubleRangeValidator.class;
        iconFileName_C16 = "DoubleRangeValidator_C16";  //NOI18N
        iconFileName_C32 = "DoubleRangeValidator_C32";  //NOI18N
        iconFileName_M16 = "DoubleRangeValidator_M16";  //NOI18N
        iconFileName_M32 = "DoubleRangeValidator_M32";  //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/core");  //NOI18N
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "validateDoubleRange");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "doubleRangeValidator");  //NOI18N
            beanDescriptor.setDisplayName(bundle.getMessage("drValid"));  //NOI18N
            beanDescriptor.setShortDescription(bundle.getMessage("drValidShortDesc"));  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_double_range_valdtr");

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

                PropertyDescriptor _minimum = new PropertyDescriptor("minimum", beanClass, "getMinimum", "setMinimum");  //NOI18N
                _minimum.setPropertyEditorClass(DoublePropertyEditor.class);

                PropertyDescriptor _maximum = new PropertyDescriptor("maximum", beanClass, "getMaximum", "setMaximum");  //NOI18N
                _maximum.setPropertyEditorClass(DoublePropertyEditor.class);

                propDescriptors = new PropertyDescriptor[] {
                    _minimum,
                    _maximum,
                };
            }
            catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }

        return propDescriptors;
    }
}
