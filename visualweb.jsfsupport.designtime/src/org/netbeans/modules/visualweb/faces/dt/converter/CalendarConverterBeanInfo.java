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
import com.sun.jsfcl.std.property.DateTimePatternPropertyEditor;
import com.sun.jsfcl.std.reference.ReferenceDataManager;
import java.beans.*;
import com.sun.rave.designtime.*;
import com.sun.rave.faces.converter.CalendarConverter;
import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public class CalendarConverterBeanInfo extends HtmlNonGeneratedBeanInfoBase {  // SimpleBeanInfo

    private static final ComponentBundle bundle = ComponentBundle.getBundle(CalendarConverterBeanInfo.class);

    /**
     * Construct a <code>CalendarConverterBeanInfo</code> instance
     */
    public CalendarConverterBeanInfo() {
        beanClass = CalendarConverter.class;
        iconFileName_C16 = "CalendarConverter_C16"; //NOI18N
        iconFileName_C32 = "CalendarConverter_C32"; //NOI18N
        iconFileName_M16 = "CalendarConverter_M16"; //NOI18N
        iconFileName_M32 = "CalendarConverter_M32"; //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/core"); //NOI18N
            //beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "convertDateTime"); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "calendarConverter"); //NOI18N
            beanDescriptor.setDisplayName(bundle.getMessage("calendarConvert")); //NOI18N
            beanDescriptor.setShortDescription(bundle.getMessage("calendarConvertShortDesc")); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_calendar_converter");
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

                PropertyDescriptor _dateStyle = new PropertyDescriptor("dateStyle", beanClass, "getDateStyle", "setDateStyle"); //NOI18N
//                _dateStyle.setShortDescription("");
                _dateStyle.setPropertyEditorClass(ChooseOneReferenceDataPropertyEditor.class);
                _dateStyle.setValue(ChooseOneReferenceDataPropertyEditor.REFERENCE_DATA_NAME, ReferenceDataManager.DATETIME_STYLES);

                PropertyDescriptor _locale = new PropertyDescriptor("locale", beanClass, "getLocale", "setLocale"); //NOI18N
//                _locale.setShortDescription("");
                _locale.setPropertyEditorClass(ChooseOneReferenceDataPropertyEditor.class);
                _locale.setValue(ChooseOneReferenceDataPropertyEditor.REFERENCE_DATA_NAME, ReferenceDataManager.LOCALES);

                PropertyDescriptor _pattern = new PropertyDescriptor("pattern", beanClass, "getPattern", "setPattern"); //NOI18N
//                _pattern.setShortDescription("");
                _pattern.setPropertyEditorClass(DateTimePatternPropertyEditor.class);

                PropertyDescriptor _timeStyle = new PropertyDescriptor("timeStyle", beanClass, "getTimeStyle", "setTimeStyle"); //NOI18N
//                _timeStyle.setShortDescription("");
                _timeStyle.setPropertyEditorClass(ChooseOneReferenceDataPropertyEditor.class);
                _timeStyle.setValue(ChooseOneReferenceDataPropertyEditor.REFERENCE_DATA_NAME, ReferenceDataManager.DATETIME_STYLES);

                PropertyDescriptor _timeZone = new PropertyDescriptor("timeZone", beanClass, "getTimeZone", "setTimeZone"); //NOI18N
//                _timeZone.setShortDescription("");
                _timeZone.setPropertyEditorClass(ChooseOneReferenceDataPropertyEditor.class);
                _timeZone.setValue(ChooseOneReferenceDataPropertyEditor.REFERENCE_DATA_NAME, ReferenceDataManager.TIME_ZONES);

                PropertyDescriptor _type = new PropertyDescriptor("type", beanClass, "getType", "setType"); //NOI18N
//                _type.setShortDescription("");
                _type.setPropertyEditorClass(ChooseOneReferenceDataPropertyEditor.class);
                _type.setValue(ChooseOneReferenceDataPropertyEditor.REFERENCE_DATA_NAME, ReferenceDataManager.DATETIME_TYPES);

                propDescriptors = new PropertyDescriptor[] {
                    _dateStyle,
                    _locale,
                    _pattern,
                    _timeStyle,
                    _timeZone,
                    _type,
                };
            }
            catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }

        return propDescriptors;
    }
}
