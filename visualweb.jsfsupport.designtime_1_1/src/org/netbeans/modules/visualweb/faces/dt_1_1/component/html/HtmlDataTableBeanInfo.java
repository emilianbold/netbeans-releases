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
import org.netbeans.modules.visualweb.faces.dt_1_1.component.UIDataBeanInfoBase;


public class HtmlDataTableBeanInfo extends UIDataBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_1.component.html.Bundle-JSF", Locale.getDefault(), HtmlDataTableBeanInfo.class.getClassLoader());


    public HtmlDataTableBeanInfo() {
        beanClass = javax.faces.component.html.HtmlDataTable.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlDataTable_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlDataTable_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlDataTable_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlDataTable_M32";
    }

    private BeanDescriptor beanDescriptor;

    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptorBase(beanClass);
        beanDescriptor.setDisplayName(resources.getString("HtmlDataTable_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("HtmlDataTable_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_jsfstd_data_table");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"dataTable");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_propsheets_jsfstd_data_table_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"dataTable");
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

            PropertyDescriptor prop_bgcolor = new PropertyDescriptorBase("bgcolor",beanClass,"getBgcolor","setBgcolor");
            prop_bgcolor.setDisplayName(resources.getString("HtmlDataTable_bgcolor_DisplayName"));
            prop_bgcolor.setShortDescription(resources.getString("HtmlDataTable_bgcolor_Description"));
            prop_bgcolor.setExpert(false);
            prop_bgcolor.setHidden(false);
            prop_bgcolor.setPreferred(false);
            attrib = new AttributeDescriptor("bgcolor",false,null,true);
            prop_bgcolor.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_bgcolor.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_border = new PropertyDescriptorBase("border",beanClass,"getBorder","setBorder");
            prop_border.setDisplayName(resources.getString("HtmlDataTable_border_DisplayName"));
            prop_border.setShortDescription(resources.getString("HtmlDataTable_border_Description"));
            prop_border.setPropertyEditorClass(com.sun.rave.propertyeditors.IntegerPropertyEditor.class);
            prop_border.setExpert(false);
            prop_border.setHidden(false);
            prop_border.setPreferred(false);
            attrib = new AttributeDescriptor("border",false,null,true);
            prop_border.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_border.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_border.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");

            PropertyDescriptor prop_cellpadding = new PropertyDescriptorBase("cellpadding",beanClass,"getCellpadding","setCellpadding");
            prop_cellpadding.setDisplayName(resources.getString("HtmlDataTable_cellpadding_DisplayName"));
            prop_cellpadding.setShortDescription(resources.getString("HtmlDataTable_cellpadding_Description"));
            prop_cellpadding.setPropertyEditorClass(com.sun.rave.propertyeditors.LengthPropertyEditor.class);
            prop_cellpadding.setExpert(false);
            prop_cellpadding.setHidden(false);
            prop_cellpadding.setPreferred(false);
            attrib = new AttributeDescriptor("cellpadding",false,null,true);
            prop_cellpadding.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_cellpadding.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_cellspacing = new PropertyDescriptorBase("cellspacing",beanClass,"getCellspacing","setCellspacing");
            prop_cellspacing.setDisplayName(resources.getString("HtmlDataTable_cellspacing_DisplayName"));
            prop_cellspacing.setShortDescription(resources.getString("HtmlDataTable_cellspacing_Description"));
            prop_cellspacing.setPropertyEditorClass(com.sun.rave.propertyeditors.LengthPropertyEditor.class);
            prop_cellspacing.setExpert(false);
            prop_cellspacing.setHidden(false);
            prop_cellspacing.setPreferred(false);
            attrib = new AttributeDescriptor("cellspacing",false,null,true);
            prop_cellspacing.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_cellspacing.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_columnClasses = new PropertyDescriptorBase("columnClasses",beanClass,"getColumnClasses","setColumnClasses");
            prop_columnClasses.setDisplayName(resources.getString("HtmlDataTable_columnClasses_DisplayName"));
            prop_columnClasses.setShortDescription(resources.getString("HtmlDataTable_columnClasses_Description"));
            prop_columnClasses.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            prop_columnClasses.setExpert(false);
            prop_columnClasses.setHidden(false);
            prop_columnClasses.setPreferred(false);
            attrib = new AttributeDescriptor("columnClasses",false,null,true);
            prop_columnClasses.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_columnClasses.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_dir = new PropertyDescriptorBase("dir",beanClass,"getDir","setDir");
            prop_dir.setDisplayName(resources.getString("HtmlDataTable_dir_DisplayName"));
            prop_dir.setShortDescription(resources.getString("HtmlDataTable_dir_Description"));
            prop_dir.setPropertyEditorClass(SelectOneDomainEditor.class);
            prop_dir.setExpert(false);
            prop_dir.setHidden(false);
            prop_dir.setPreferred(false);
            attrib = new AttributeDescriptor("dir",false,null,true);
            prop_dir.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dir.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_dir.setValue(DomainPropertyEditor.DOMAIN_CLASS, TextDirectionDomain.class);

            PropertyDescriptor prop_footerClass = new PropertyDescriptorBase("footerClass",beanClass,"getFooterClass","setFooterClass");
            prop_footerClass.setDisplayName(resources.getString("HtmlDataTable_footerClass_DisplayName"));
            prop_footerClass.setShortDescription(resources.getString("HtmlDataTable_footerClass_Description"));
            prop_footerClass.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            prop_footerClass.setExpert(false);
            prop_footerClass.setHidden(false);
            prop_footerClass.setPreferred(false);
            attrib = new AttributeDescriptor("footerClass",false,null,true);
            prop_footerClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_footerClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_frame = new PropertyDescriptorBase("frame",beanClass,"getFrame","setFrame");
            prop_frame.setDisplayName(resources.getString("HtmlDataTable_frame_DisplayName"));
            prop_frame.setShortDescription(resources.getString("HtmlDataTable_frame_Description"));
            prop_frame.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_frame.setExpert(false);
            prop_frame.setHidden(false);
            prop_frame.setPreferred(false);
            attrib = new AttributeDescriptor("frame",false,null,true);
            prop_frame.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_frame.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_frame.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlTableBordersDomain.class);

            PropertyDescriptor prop_headerClass = new PropertyDescriptorBase("headerClass",beanClass,"getHeaderClass","setHeaderClass");
            prop_headerClass.setDisplayName(resources.getString("HtmlDataTable_headerClass_DisplayName"));
            prop_headerClass.setShortDescription(resources.getString("HtmlDataTable_headerClass_Description"));
            prop_headerClass.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            prop_headerClass.setExpert(false);
            prop_headerClass.setHidden(false);
            prop_headerClass.setPreferred(false);
            attrib = new AttributeDescriptor("headerClass",false,null,true);
            prop_headerClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_headerClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_lang = new PropertyDescriptorBase("lang",beanClass,"getLang","setLang");
            prop_lang.setDisplayName(resources.getString("HtmlDataTable_lang_DisplayName"));
            prop_lang.setShortDescription(resources.getString("HtmlDataTable_lang_Description"));
            prop_lang.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_lang.setExpert(false);
            prop_lang.setHidden(false);
            prop_lang.setPreferred(false);
            attrib = new AttributeDescriptor("lang",false,null,true);
            prop_lang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_lang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_lang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_rowClasses = new PropertyDescriptorBase("rowClasses",beanClass,"getRowClasses","setRowClasses");
            prop_rowClasses.setDisplayName(resources.getString("HtmlDataTable_rowClasses_DisplayName"));
            prop_rowClasses.setShortDescription(resources.getString("HtmlDataTable_rowClasses_Description"));
            prop_rowClasses.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            prop_rowClasses.setExpert(false);
            prop_rowClasses.setHidden(false);
            prop_rowClasses.setPreferred(false);
            attrib = new AttributeDescriptor("rowClasses",false,null,true);
            prop_rowClasses.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rowClasses.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_rules = new PropertyDescriptorBase("rules",beanClass,"getRules","setRules");
            prop_rules.setDisplayName(resources.getString("HtmlDataTable_rules_DisplayName"));
            prop_rules.setShortDescription(resources.getString("HtmlDataTable_rules_Description"));
            prop_rules.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_rules.setExpert(false);
            prop_rules.setHidden(false);
            prop_rules.setPreferred(false);
            attrib = new AttributeDescriptor("rules",false,null,true);
            prop_rules.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rules.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_rules.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlTableRulesDomain.class);

            PropertyDescriptor prop_summary = new PropertyDescriptorBase("summary",beanClass,"getSummary","setSummary");
            prop_summary.setDisplayName(resources.getString("HtmlDataTable_summary_DisplayName"));
            prop_summary.setShortDescription(resources.getString("HtmlDataTable_summary_Description"));
            prop_summary.setExpert(false);
            prop_summary.setHidden(false);
            prop_summary.setPreferred(false);
            attrib = new AttributeDescriptor("summary",false,null,true);
            prop_summary.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_summary.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_title = new PropertyDescriptorBase("title",beanClass,"getTitle","setTitle");
            prop_title.setDisplayName(resources.getString("HtmlDataTable_title_DisplayName"));
            prop_title.setShortDescription(resources.getString("HtmlDataTable_title_Description"));
            prop_title.setExpert(false);
            prop_title.setHidden(false);
            prop_title.setPreferred(false);
            attrib = new AttributeDescriptor("title",false,null,true);
            prop_title.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_title.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_width = new PropertyDescriptorBase("width",beanClass,"getWidth","setWidth");
            prop_width.setDisplayName(resources.getString("HtmlDataTable_width_DisplayName"));
            prop_width.setShortDescription(resources.getString("HtmlDataTable_width_Description"));
            prop_width.setPropertyEditorClass(com.sun.rave.propertyeditors.LengthPropertyEditor.class);
            prop_width.setExpert(false);
            prop_width.setHidden(false);
            prop_width.setPreferred(false);
            attrib = new AttributeDescriptor("width",false,null,true);
            prop_width.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_width.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_bgcolor);
            propertyDescriptorList.add(prop_border);
            propertyDescriptorList.add(prop_cellpadding);
            propertyDescriptorList.add(prop_cellspacing);
            propertyDescriptorList.add(prop_columnClasses);
            propertyDescriptorList.add(prop_dir);
            propertyDescriptorList.add(prop_footerClass);
            propertyDescriptorList.add(prop_frame);
            propertyDescriptorList.add(prop_headerClass);
            propertyDescriptorList.add(prop_lang);
            propertyDescriptorList.add(prop_rowClasses);
            propertyDescriptorList.add(prop_rules);
            propertyDescriptorList.add(prop_summary);
            propertyDescriptorList.add(prop_title);
            propertyDescriptorList.add(prop_width);

            propertyDescriptorList.addAll(Properties.getVisualPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getKeyEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getMouseEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Properties.getClickEventPropertyList(beanClass));
            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}

