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
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.faces.dt.BeanDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt_1_2.component.UIOutputBeanInfoBase;


public class HtmlOutputLabelBeanInfo extends UIOutputBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_2.component.html.Bundle-JSF", Locale.getDefault(), HtmlOutputLabelBeanInfo.class.getClassLoader());


    public HtmlOutputLabelBeanInfo() {
        beanClass = javax.faces.component.html.HtmlOutputLabel.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlOutputLabel_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlOutputLabel_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlOutputLabel_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/html/HtmlOutputLabel_M32";
    }


    private BeanDescriptor beanDescriptor;

    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptorBase(beanClass);
        beanDescriptor.setDisplayName(resources.getString("HtmlOutputLabel_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("HtmlOutputLabel_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_jsfstd_component_label");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"componentLabel");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_propsheets_jsfstd_component_label_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"outputLabel");
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

            PropertyDescriptor prop_accesskey = new PropertyDescriptorBase("accesskey",beanClass,"getAccesskey","setAccesskey");
            prop_accesskey.setDisplayName(resources.getString("HtmlOutputLabel_accesskey_DisplayName"));
            prop_accesskey.setShortDescription(resources.getString("HtmlOutputLabel_accesskey_Description"));
            prop_accesskey.setExpert(false);
            prop_accesskey.setHidden(false);
            prop_accesskey.setPreferred(false);
            attrib = new AttributeDescriptor("accesskey",false,null,true);
            prop_accesskey.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_accesskey.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_dir = new PropertyDescriptorBase("dir",beanClass,"getDir","setDir");
            prop_dir.setDisplayName(resources.getString("HtmlOutputLabel_dir_DisplayName"));
            prop_dir.setShortDescription(resources.getString("HtmlOutputLabel_dir_Description"));
            prop_dir.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_dir.setExpert(false);
            prop_dir.setHidden(false);
            prop_dir.setPreferred(false);
            attrib = new AttributeDescriptor("dir",false,null,true);
            prop_dir.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dir.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_dir.setValue(com.sun.rave.propertyeditors.DomainPropertyEditor.DOMAIN_CLASS, com.sun.rave.propertyeditors.domains.TextDirectionDomain.class);

            PropertyDescriptor prop_for = new PropertyDescriptorBase("for",beanClass,"getFor","setFor");
            prop_for.setDisplayName(resources.getString("HtmlOutputLabel_for_DisplayName"));
            prop_for.setShortDescription(resources.getString("HtmlOutputLabel_for_Description"));
            prop_for.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_for.setExpert(false);
            prop_for.setHidden(false);
            prop_for.setPreferred(false);
            attrib = new AttributeDescriptor("for",false,null,true);
            prop_for.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_for.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_for.setValue(com.sun.rave.propertyeditors.DomainPropertyEditor.DOMAIN_CLASS, com.sun.rave.propertyeditors.domains.InputComponentIdsDomain.class);

            PropertyDescriptor prop_lang = new PropertyDescriptorBase("lang",beanClass,"getLang","setLang");
            prop_lang.setDisplayName(resources.getString("HtmlOutputLabel_lang_DisplayName"));
            prop_lang.setShortDescription(resources.getString("HtmlOutputLabel_lang_Description"));
            prop_lang.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_lang.setExpert(false);
            prop_lang.setHidden(false);
            prop_lang.setPreferred(false);
            attrib = new AttributeDescriptor("lang",false,null,true);
            prop_lang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_lang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_lang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_tabindex = new PropertyDescriptorBase("tabindex",beanClass,"getTabindex","setTabindex");
            prop_tabindex.setDisplayName(resources.getString("HtmlOutputLabel_tabindex_DisplayName"));
            prop_tabindex.setShortDescription(resources.getString("HtmlOutputLabel_tabindex_Description"));
            prop_tabindex.setPropertyEditorClass(com.sun.rave.propertyeditors.IntegerPropertyEditor.class);
            prop_tabindex.setExpert(false);
            prop_tabindex.setHidden(false);
            prop_tabindex.setPreferred(false);
            attrib = new AttributeDescriptor("tabindex",false,null,true);
            prop_tabindex.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tabindex.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_tabindex.setValue("maxValue", new Integer(Short.MAX_VALUE));
            prop_tabindex.setValue("minValue", new Integer(0));

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_accesskey);
            propertyDescriptorList.add(prop_dir);
            propertyDescriptorList.add(prop_for);
            propertyDescriptorList.add(prop_lang);
            propertyDescriptorList.add(prop_tabindex);

            propertyDescriptorList.addAll(Properties.getVisualPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getKeyEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getMouseEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getClickEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getFocusEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}

