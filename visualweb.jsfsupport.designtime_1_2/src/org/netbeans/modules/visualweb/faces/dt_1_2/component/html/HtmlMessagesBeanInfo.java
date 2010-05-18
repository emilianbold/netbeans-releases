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

package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.faces.FacetDescriptor;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.faces.dt.BeanDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt_1_2.component.UIMessagesBeanInfoBase;


public class HtmlMessagesBeanInfo extends UIMessagesBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_2.component.html.Bundle-JSF", Locale.getDefault(), HtmlMessagesBeanInfo.class.getClassLoader());


    public HtmlMessagesBeanInfo() {
        beanClass = javax.faces.component.html.HtmlMessages.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlMessages_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlMessages_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlMessages_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlMessages_M32";
    }


    private BeanDescriptor beanDescriptor;

    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptorBase(beanClass);
        beanDescriptor.setDisplayName(resources.getString("HtmlMessages_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("HtmlMessages_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_jsfstd_message_list");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"messageList");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.FALSE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_propsheets_jsfstd_message_list_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"messages");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_PREFIX,"h");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI,"http://java.sun.com/jsf/html");

        return beanDescriptor;

    }


    private PropertyDescriptor[] propertyDescriptors;

    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_errorClass = new PropertyDescriptorBase("errorClass",beanClass,"getErrorClass","setErrorClass");
            prop_errorClass.setDisplayName(resources.getString("HtmlMessages_errorClass_DisplayName"));
            prop_errorClass.setShortDescription(resources.getString("HtmlMessages_errorClass_Description"));
            prop_errorClass.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            prop_errorClass.setExpert(false);
            prop_errorClass.setHidden(false);
            prop_errorClass.setPreferred(false);
            attrib = new AttributeDescriptor("errorClass",false,null,true);
            prop_errorClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_errorClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_errorStyle = new PropertyDescriptorBase("errorStyle",beanClass,"getErrorStyle","setErrorStyle");
            prop_errorStyle.setDisplayName(resources.getString("HtmlMessages_errorStyle_DisplayName"));
            prop_errorStyle.setShortDescription(resources.getString("HtmlMessages_errorStyle_Description"));
            prop_errorStyle.setPropertyEditorClass(com.sun.rave.propertyeditors.css.CssStylePropertyEditor.class);
            prop_errorStyle.setExpert(false);
            prop_errorStyle.setHidden(false);
            prop_errorStyle.setPreferred(false);
            attrib = new AttributeDescriptor("errorStyle",false,null,true);
            prop_errorStyle.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_errorStyle.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_fatalClass = new PropertyDescriptorBase("fatalClass",beanClass,"getFatalClass","setFatalClass");
            prop_fatalClass.setDisplayName(resources.getString("HtmlMessages_fatalClass_DisplayName"));
            prop_fatalClass.setShortDescription(resources.getString("HtmlMessages_fatalClass_Description"));
            prop_fatalClass.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            prop_fatalClass.setExpert(false);
            prop_fatalClass.setHidden(false);
            prop_fatalClass.setPreferred(false);
            attrib = new AttributeDescriptor("fatalClass",false,null,true);
            prop_fatalClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_fatalClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_fatalStyle = new PropertyDescriptorBase("fatalStyle",beanClass,"getFatalStyle","setFatalStyle");
            prop_fatalStyle.setDisplayName(resources.getString("HtmlMessages_fatalStyle_DisplayName"));
            prop_fatalStyle.setShortDescription(resources.getString("HtmlMessages_fatalStyle_Description"));
            prop_fatalStyle.setPropertyEditorClass(com.sun.rave.propertyeditors.css.CssStylePropertyEditor.class);
            prop_fatalStyle.setExpert(false);
            prop_fatalStyle.setHidden(false);
            prop_fatalStyle.setPreferred(false);
            attrib = new AttributeDescriptor("fatalStyle",false,null,true);
            prop_fatalStyle.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_fatalStyle.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_infoClass = new PropertyDescriptorBase("infoClass",beanClass,"getInfoClass","setInfoClass");
            prop_infoClass.setDisplayName(resources.getString("HtmlMessages_infoClass_DisplayName"));
            prop_infoClass.setShortDescription(resources.getString("HtmlMessages_infoClass_Description"));
            prop_infoClass.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            prop_infoClass.setExpert(false);
            prop_infoClass.setHidden(false);
            prop_infoClass.setPreferred(false);
            attrib = new AttributeDescriptor("infoClass",false,null,true);
            prop_infoClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_infoClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_infoStyle = new PropertyDescriptorBase("infoStyle",beanClass,"getInfoStyle","setInfoStyle");
            prop_infoStyle.setDisplayName(resources.getString("HtmlMessages_infoStyle_DisplayName"));
            prop_infoStyle.setShortDescription(resources.getString("HtmlMessages_infoStyle_Description"));
            prop_infoStyle.setPropertyEditorClass(com.sun.rave.propertyeditors.css.CssStylePropertyEditor.class);
            prop_infoStyle.setExpert(false);
            prop_infoStyle.setHidden(false);
            prop_infoStyle.setPreferred(false);
            attrib = new AttributeDescriptor("infoStyle",false,null,true);
            prop_infoStyle.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_infoStyle.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_layout = new PropertyDescriptorBase("layout",beanClass,"getLayout","setLayout");
            prop_layout.setDisplayName(resources.getString("HtmlMessages_layout_DisplayName"));
            prop_layout.setShortDescription(resources.getString("HtmlMessages_layout_Description"));
            prop_layout.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_layout.setExpert(false);
            prop_layout.setHidden(false);
            prop_layout.setPreferred(false);
            attrib = new AttributeDescriptor("layout",false,"\"list\"",true);
            prop_layout.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_layout.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_layout.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", org.netbeans.modules.visualweb.faces.dt_1_2.component.html.HtmlMessagesLayoutStylesDomain.class);

            PropertyDescriptor prop_tooltip = new PropertyDescriptorBase("tooltip",beanClass,"isTooltip","setTooltip");
            prop_tooltip.setDisplayName(resources.getString("HtmlMessages_tooltip_DisplayName"));
            prop_tooltip.setShortDescription(resources.getString("HtmlMessages_tooltip_Description"));
            prop_tooltip.setExpert(false);
            prop_tooltip.setHidden(false);
            prop_tooltip.setPreferred(false);
            attrib = new AttributeDescriptor("tooltip",false,null,true);
            prop_tooltip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tooltip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_warnClass = new PropertyDescriptorBase("warnClass",beanClass,"getWarnClass","setWarnClass");
            prop_warnClass.setDisplayName(resources.getString("HtmlMessages_warnClass_DisplayName"));
            prop_warnClass.setShortDescription(resources.getString("HtmlMessages_warnClass_Description"));
            prop_warnClass.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            prop_warnClass.setExpert(false);
            prop_warnClass.setHidden(false);
            prop_warnClass.setPreferred(false);
            attrib = new AttributeDescriptor("warnClass",false,null,true);
            prop_warnClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_warnClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_warnStyle = new PropertyDescriptorBase("warnStyle",beanClass,"getWarnStyle","setWarnStyle");
            prop_warnStyle.setDisplayName(resources.getString("HtmlMessages_warnStyle_DisplayName"));
            prop_warnStyle.setShortDescription(resources.getString("HtmlMessages_warnStyle_Description"));
            prop_warnStyle.setPropertyEditorClass(com.sun.rave.propertyeditors.css.CssStylePropertyEditor.class);
            prop_warnStyle.setExpert(false);
            prop_warnStyle.setHidden(false);
            prop_warnStyle.setPreferred(false);
            attrib = new AttributeDescriptor("warnStyle",false,null,true);
            prop_warnStyle.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_warnStyle.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_errorClass);
            propertyDescriptorList.add(prop_errorStyle);
            propertyDescriptorList.add(prop_fatalClass);
            propertyDescriptorList.add(prop_fatalStyle);
            propertyDescriptorList.add(prop_infoClass);
            propertyDescriptorList.add(prop_infoStyle);
            propertyDescriptorList.add(prop_layout);
            propertyDescriptorList.add(prop_tooltip);
            propertyDescriptorList.add(prop_warnClass);
            propertyDescriptorList.add(prop_warnStyle);

            propertyDescriptorList.addAll(Properties.getVisualPropertyList(beanClass));
            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}

