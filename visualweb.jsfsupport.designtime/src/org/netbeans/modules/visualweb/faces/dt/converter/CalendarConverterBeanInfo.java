/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.faces.dt.converter;

import com.sun.rave.propertyeditors.DomainPropertyEditor;
import java.beans.*;
import com.sun.rave.designtime.*;
import com.sun.rave.faces.converter.CalendarConverter;
import com.sun.rave.propertyeditors.SelectOneDomainEditor;
import com.sun.rave.propertyeditors.domains.LocalesDomain;
import com.sun.rave.propertyeditors.domains.TimeZonesDomain;
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
