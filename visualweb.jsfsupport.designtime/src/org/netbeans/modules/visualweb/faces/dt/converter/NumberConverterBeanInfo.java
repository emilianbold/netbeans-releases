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
package org.netbeans.modules.visualweb.faces.dt.converter;

import com.sun.jsfcl.std.property.ChooseOneReferenceDataPropertyEditor;
import com.sun.jsfcl.std.property.NumberConverterTypePropertyEditor;
import com.sun.jsfcl.std.property.NumberPatternPropertyEditor;
import com.sun.jsfcl.std.reference.ReferenceDataManager;
import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import java.beans.*;
import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.propertyeditors.IntegerPropertyEditor;
import javax.faces.convert.NumberConverter;

public class NumberConverterBeanInfo extends HtmlNonGeneratedBeanInfoBase {  // SimpleBeanInfo

    private static final ComponentBundle bundle = ComponentBundle.getBundle(NumberConverterBeanInfo.class);

    /**
     * Construct a <code>NumberConverterBeanInfo</code> instance
     */
    public NumberConverterBeanInfo() {
        beanClass = NumberConverter.class;
        iconFileName_C16 = "NumberConverter_C16";   //NOI18N
        iconFileName_C32 = "NumberConverter_C32";   //NOI18N
        iconFileName_M16 = "NumberConverter_M16";   //NOI18N
        iconFileName_M32 = "NumberConverter_M32";   //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/core");   //NOI18N
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "convertNumber");   //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "numberConverter");   //NOI18N
            beanDescriptor.setDisplayName(bundle.getMessage("numConvert"));   //NOI18N
            beanDescriptor.setShortDescription(bundle.getMessage("numConvertShortDesc"));   //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_number_converter");
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

                PropertyDescriptor _currencyCode = new PropertyDescriptor("currencyCode", beanClass, "getCurrencyCode", "setCurrencyCode");   //NOI18N
//                _currencyCode.setShortDescription("");

                PropertyDescriptor _currencySymbol = new PropertyDescriptor("currencySymbol", beanClass, "getCurrencySymbol", "setCurrencySymbol");   //NOI18N
//                _currencySymbol.setShortDescription("");

                PropertyDescriptor _integerOnly = new PropertyDescriptor("integerOnly", beanClass, "isIntegerOnly", "setIntegerOnly");   //NOI18N
//                _integerOnly.setShortDescription("");

                 PropertyDescriptor _groupingUsed = new PropertyDescriptor("groupingUsed", beanClass, "isGroupingUsed", "setGroupingUsed");   //NOI18N
//                _integerOnly.setShortDescription("");

                PropertyDescriptor _maxFractionDigits = new PropertyDescriptor("maxFractionDigits", beanClass, "getMaxFractionDigits", "setMaxFractionDigits");   //NOI18N
//                _maxFractionDigits.setShortDescription("");
                _maxFractionDigits.setPropertyEditorClass(IntegerPropertyEditor.class);
                _maxFractionDigits.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");

                PropertyDescriptor _minFractionDigits = new PropertyDescriptor("minFractionDigits", beanClass, "getMinFractionDigits", "setMinFractionDigits");   //NOI18N
//                _minFractionDigits.setShortDescription("");
                _minFractionDigits.setPropertyEditorClass(IntegerPropertyEditor.class);
                _minFractionDigits.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");             

                PropertyDescriptor _maxIntegerDigits = new PropertyDescriptor("maxIntegerDigits", beanClass, "getMaxIntegerDigits", "setMaxIntegerDigits");   //NOI18N
//                _maxIntegerDigits.setShortDescription("");
                _maxIntegerDigits.setPropertyEditorClass(IntegerPropertyEditor.class);
                _maxIntegerDigits.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");               

                PropertyDescriptor _minIntegerDigits = new PropertyDescriptor("minIntegerDigits", beanClass, "getMinIntegerDigits", "setMinIntegerDigits");   //NOI18N
//                _minIntegerDigits.setShortDescription("");
                _minIntegerDigits.setPropertyEditorClass(IntegerPropertyEditor.class);
                _minIntegerDigits.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");     
                
                PropertyDescriptor _locale = new PropertyDescriptor("locale", beanClass, "getLocale", "setLocale");   //NOI18N
//                _locale.setShortDescription("");
                _locale.setPropertyEditorClass(ChooseOneReferenceDataPropertyEditor.class);
                _locale.setValue(ChooseOneReferenceDataPropertyEditor.REFERENCE_DATA_NAME, ReferenceDataManager.LOCALES);

                PropertyDescriptor _pattern = new PropertyDescriptor("pattern", beanClass, "getPattern", "setPattern");   //NOI18N
//                _pattern.setShortDescription("");
                _pattern.setPropertyEditorClass(NumberPatternPropertyEditor.class);

                PropertyDescriptor _type = new PropertyDescriptor("type", beanClass, "getType", "setType");   //NOI18N
//                _type.setShortDescription("");
                _type.setPropertyEditorClass(NumberConverterTypePropertyEditor.class);
                

                propDescriptors = new PropertyDescriptor[] {
                    _currencyCode,
                    _currencySymbol,
                    _integerOnly,
                    _groupingUsed,
                    _maxFractionDigits,
                    _minFractionDigits,
                    _maxIntegerDigits,
                    _minIntegerDigits,
                    _locale,
                    _pattern,
                    _type,
                };
            } catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }

        return propDescriptors;
    }
}
