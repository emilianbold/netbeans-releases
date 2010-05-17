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

package org.netbeans.modules.visualweb.faces.dt_1_1.component.html;

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
import com.sun.rave.propertyeditors.DomainPropertyEditor;
import com.sun.rave.propertyeditors.SelectOneDomainEditor;
import com.sun.rave.propertyeditors.domains.TextDirectionDomain;
import org.netbeans.modules.visualweb.faces.dt.BeanDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt_1_1.component.UIInputBeanInfoBase;

public class HtmlInputSecretBeanInfo extends UIInputBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_1.component.html.Bundle-JSF", Locale.getDefault(), HtmlInputSecretBeanInfo.class.getClassLoader());


    public HtmlInputSecretBeanInfo() {
        beanClass = javax.faces.component.html.HtmlInputSecret.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlInputSecret_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlInputSecret_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlInputSecret_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlInputSecret_M32";
    }


    private BeanDescriptor beanDescriptor;

    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptorBase(beanClass);
        beanDescriptor.setDisplayName(resources.getString("HtmlInputSecret_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("HtmlInputSecret_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_jsfstd_secret_field");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"secretField");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.FALSE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_propsheets_jsfstd_secret_field_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"inputSecret");
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
            prop_accesskey.setDisplayName(resources.getString("HtmlInputSecret_accesskey_DisplayName"));
            prop_accesskey.setShortDescription(resources.getString("HtmlInputSecret_accesskey_Description"));
            prop_accesskey.setExpert(false);
            prop_accesskey.setHidden(false);
            prop_accesskey.setPreferred(false);
            attrib = new AttributeDescriptor("accesskey",false,null,true);
            prop_accesskey.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_accesskey.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_alt = new PropertyDescriptorBase("alt",beanClass,"getAlt","setAlt");
            prop_alt.setDisplayName(resources.getString("HtmlInputSecret_alt_DisplayName"));
            prop_alt.setShortDescription(resources.getString("HtmlInputSecret_alt_Description"));
            prop_alt.setExpert(false);
            prop_alt.setHidden(false);
            prop_alt.setPreferred(false);
            attrib = new AttributeDescriptor("alt",false,null,true);
            prop_alt.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_alt.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_dir = new PropertyDescriptorBase("dir",beanClass,"getDir","setDir");
            prop_dir.setDisplayName(resources.getString("HtmlInputSecret_dir_DisplayName"));
            prop_dir.setShortDescription(resources.getString("HtmlInputSecret_dir_Description"));
            prop_dir.setPropertyEditorClass(SelectOneDomainEditor.class);
            prop_dir.setExpert(false);
            prop_dir.setHidden(false);
            prop_dir.setPreferred(false);
            attrib = new AttributeDescriptor("dir",false,null,true);
            prop_dir.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dir.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_dir.setValue(DomainPropertyEditor.DOMAIN_CLASS, TextDirectionDomain.class);

            PropertyDescriptor prop_disabled = new PropertyDescriptorBase("disabled",beanClass,"isDisabled","setDisabled");
            prop_disabled.setDisplayName(resources.getString("HtmlInputSecret_disabled_DisplayName"));
            prop_disabled.setShortDescription(resources.getString("HtmlInputSecret_disabled_Description"));
            prop_disabled.setExpert(false);
            prop_disabled.setHidden(false);
            prop_disabled.setPreferred(false);
            attrib = new AttributeDescriptor("disabled",false,null,true);
            prop_disabled.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_disabled.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_lang = new PropertyDescriptorBase("lang",beanClass,"getLang","setLang");
            prop_lang.setDisplayName(resources.getString("HtmlInputSecret_lang_DisplayName"));
            prop_lang.setShortDescription(resources.getString("HtmlInputSecret_lang_Description"));
            prop_lang.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_lang.setExpert(false);
            prop_lang.setHidden(false);
            prop_lang.setPreferred(false);
            attrib = new AttributeDescriptor("lang",false,null,true);
            prop_lang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_lang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_lang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_maxlength = new PropertyDescriptorBase("maxlength",beanClass,"getMaxlength","setMaxlength");
            prop_maxlength.setDisplayName(resources.getString("HtmlInputSecret_maxlength_DisplayName"));
            prop_maxlength.setShortDescription(resources.getString("HtmlInputSecret_maxlength_Description"));
            prop_maxlength.setPropertyEditorClass(com.sun.rave.propertyeditors.IntegerPropertyEditor.class);
            prop_maxlength.setExpert(false);
            prop_maxlength.setHidden(false);
            prop_maxlength.setPreferred(false);
            attrib = new AttributeDescriptor("maxlength",false,null,true);
            prop_maxlength.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_maxlength.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_maxlength.setValue("minValue", new Integer(0));
            prop_maxlength.setValue("unsetValue", new Integer(Integer.MIN_VALUE));

            PropertyDescriptor prop_readonly = new PropertyDescriptorBase("readonly",beanClass,"isReadonly","setReadonly");
            prop_readonly.setDisplayName(resources.getString("HtmlInputSecret_readonly_DisplayName"));
            prop_readonly.setShortDescription(resources.getString("HtmlInputSecret_readonly_Description"));
            prop_readonly.setExpert(false);
            prop_readonly.setHidden(false);
            prop_readonly.setPreferred(false);
            attrib = new AttributeDescriptor("readonly",false,null,true);
            prop_readonly.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_readonly.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_redisplay = new PropertyDescriptorBase("redisplay",beanClass,"isRedisplay","setRedisplay");
            prop_redisplay.setDisplayName(resources.getString("HtmlInputSecret_redisplay_DisplayName"));
            prop_redisplay.setShortDescription(resources.getString("HtmlInputSecret_redisplay_Description"));
            prop_redisplay.setExpert(false);
            prop_redisplay.setHidden(false);
            prop_redisplay.setPreferred(false);
            attrib = new AttributeDescriptor("redisplay",false,null,true);
            prop_redisplay.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_redisplay.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_size = new PropertyDescriptorBase("size",beanClass,"getSize","setSize");
            prop_size.setDisplayName(resources.getString("HtmlInputSecret_size_DisplayName"));
            prop_size.setShortDescription(resources.getString("HtmlInputSecret_size_Description"));
            prop_size.setPropertyEditorClass(com.sun.rave.propertyeditors.IntegerPropertyEditor.class);
            prop_size.setExpert(false);
            prop_size.setHidden(false);
            prop_size.setPreferred(false);
            attrib = new AttributeDescriptor("size",false,null,true);
            prop_size.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_size.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_size.setValue("minValue", new Integer(0));
            prop_size.setValue("unsetValue", new Integer(Integer.MIN_VALUE));

            PropertyDescriptor prop_tabindex = new PropertyDescriptorBase("tabindex",beanClass,"getTabindex","setTabindex");
            prop_tabindex.setDisplayName(resources.getString("HtmlInputSecret_tabindex_DisplayName"));
            prop_tabindex.setShortDescription(resources.getString("HtmlInputSecret_tabindex_Description"));
            prop_tabindex.setPropertyEditorClass(com.sun.rave.propertyeditors.IntegerPropertyEditor.class);
            prop_tabindex.setExpert(false);
            prop_tabindex.setHidden(false);
            prop_tabindex.setPreferred(false);
            attrib = new AttributeDescriptor("tabindex",false,null,true);
            prop_tabindex.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tabindex.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_tabindex.setValue("maxValue", new Integer(Short.MAX_VALUE));
            prop_tabindex.setValue("minValue", new Integer(0));

            PropertyDescriptor prop_title = new PropertyDescriptorBase("title",beanClass,"getTitle","setTitle");
            prop_title.setDisplayName(resources.getString("HtmlInputSecret_title_DisplayName"));
            prop_title.setShortDescription(resources.getString("HtmlInputSecret_title_Description"));
            prop_title.setExpert(false);
            prop_title.setHidden(false);
            prop_title.setPreferred(false);
            attrib = new AttributeDescriptor("title",false,null,true);
            prop_title.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_title.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_accesskey);
            propertyDescriptorList.add(prop_alt);
            propertyDescriptorList.add(prop_dir);
            propertyDescriptorList.add(prop_disabled);
            propertyDescriptorList.add(prop_lang);
            propertyDescriptorList.add(prop_maxlength);
            propertyDescriptorList.add(prop_readonly);
            propertyDescriptorList.add(prop_redisplay);
            propertyDescriptorList.add(prop_size);
            propertyDescriptorList.add(prop_tabindex);
            propertyDescriptorList.add(prop_title);

            propertyDescriptorList.addAll(Properties.getVisualPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getKeyEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getMouseEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getClickEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getFocusEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getSelectEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getChangeEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}

