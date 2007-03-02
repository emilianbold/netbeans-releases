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

import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import java.beans.*;
import com.sun.rave.designtime.*;
import com.sun.rave.propertyeditors.DomainPropertyEditor;
import com.sun.rave.propertyeditors.SelectOneDomainEditor;
import com.sun.rave.propertyeditors.domains.LocalesDomain;
import com.sun.rave.propertyeditors.domains.TimeZonesDomain;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.convert.DateTimeConverter;

public class DateTimeConverterBeanInfo extends HtmlNonGeneratedBeanInfoBase {  // SimpleBeanInfo

    private static final ComponentBundle bundle = ComponentBundle.getBundle(DateTimeConverterBeanInfo.class);

    /**
     * Construct a <code>DateTimeConverterBeanInfo</code> instance
     */
    public DateTimeConverterBeanInfo() {
        beanClass = DateTimeConverter.class;
        iconFileName_C16 = "DateTimeConverter_C16"; //NOI18N
        iconFileName_C32 = "DateTimeConverter_C32"; //NOI18N
        iconFileName_M16 = "DateTimeConverter_M16"; //NOI18N
        iconFileName_M32 = "DateTimeConverter_M32"; //NOI18N
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
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "dateTimeConverter"); //NOI18N
            beanDescriptor.setDisplayName(bundle.getMessage("dtConvert")); //NOI18N
            beanDescriptor.setShortDescription(bundle.getMessage("dtConvertShortDesc")); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_date_time_converter");
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
                _dateStyle.setPropertyEditorClass(SelectOneDomainEditor.class);
                _dateStyle.setValue(DomainPropertyEditor.DOMAIN_CLASS, DateTimeStylesDomain.class);

                PropertyDescriptor _locale = new PropertyDescriptor("locale", beanClass, "getLocale", "setLocale"); //NOI18N
//                _locale.setShortDescription("");
                _locale.setPropertyEditorClass(SelectOneDomainEditor.class);
                _locale.setValue(DomainPropertyEditor.DOMAIN_CLASS, LocalesDomain.class);

                PropertyDescriptor _pattern = new PropertyDescriptor("pattern", beanClass, "getPattern", "setPattern"); //NOI18N
//                _pattern.setShortDescription("");
                _pattern.setPropertyEditorClass(DateTimePatternPropertyEditor.class);

                PropertyDescriptor _timeStyle = new PropertyDescriptor("timeStyle", beanClass, "getTimeStyle", "setTimeStyle"); //NOI18N
//                _timeStyle.setShortDescription("");
                _timeStyle.setPropertyEditorClass(SelectOneDomainEditor.class);
                _timeStyle.setValue(DomainPropertyEditor.DOMAIN_CLASS, DateTimeStylesDomain.class);

                PropertyDescriptor _timeZone = new PropertyDescriptor("timeZone", beanClass, "getTimeZone", "setTimeZone"); //NOI18N
//                _timeZone.setShortDescription("");
                _timeZone.setPropertyEditorClass(SelectOneDomainEditor.class);
                _timeZone.setValue(DomainPropertyEditor.DOMAIN_CLASS, TimeZonesDomain.class);

                PropertyDescriptor _type = new PropertyDescriptor("type", beanClass, "getType", "setType"); //NOI18N
//                _type.setShortDescription("");
                _type.setPropertyEditorClass(SelectOneDomainEditor.class);
                _type.setValue(DomainPropertyEditor.DOMAIN_CLASS, DateTimeTypesDomain.class);

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
