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

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.base.CategoryDescriptors;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for generating common component properties.
 */
class Properties {

    private static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_2.component.html.Bundle-JSF", Locale.getDefault(),
            Properties.class.getClassLoader());

    static List<PropertyDescriptor> getVisualPropertyList(Class beanClass) {

        try {
            AttributeDescriptor attrib;

            PropertyDescriptor prop_style = new PropertyDescriptorBase("style", beanClass, "getStyle", "setStyle");
            prop_style.setDisplayName(resources.getString("Properties_style_DisplayName"));
            prop_style.setShortDescription(resources.getString("Properties_style_Description"));
            prop_style.setPropertyEditorClass(com.sun.rave.propertyeditors.css.CssStylePropertyEditor.class);
            attrib = new AttributeDescriptor("style", false, null, true);
            prop_style.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib);
            prop_style.setValue(Constants.PropertyDescriptor.CATEGORY, CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_styleClass = new PropertyDescriptorBase("styleClass", beanClass, "getStyleClass", "setStyleClass");
            prop_styleClass.setDisplayName(resources.getString("Properties_styleClass_DisplayName"));
            prop_styleClass.setShortDescription(resources.getString("Properties_styleClass_Description"));
            prop_styleClass.setPropertyEditorClass(com.sun.rave.propertyeditors.StyleClassPropertyEditor.class);
            attrib = new AttributeDescriptor("styleClass", false, null, true);
            prop_styleClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib);
            prop_styleClass.setValue(Constants.PropertyDescriptor.CATEGORY, CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_title = new PropertyDescriptorBase("title",beanClass,"getTitle","setTitle");
            prop_title.setDisplayName(resources.getString("Properties_title_DisplayName"));
            prop_title.setShortDescription(resources.getString("Properties_title_Description"));
            attrib = new AttributeDescriptor("title",false,null,true);
            prop_title.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_title.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
            propertyList.add(prop_style);
            propertyList.add(prop_styleClass);
            propertyList.add(prop_title);
            return propertyList;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;

    }

    static List<PropertyDescriptor> getInputPropertyList(Class beanClass) {

        try {
            AttributeDescriptor attrib;

            PropertyDescriptor prop_converterMessage = new PropertyDescriptorBase("converterMessage",beanClass,"getConverterMessage","setConverterMessage");
            prop_converterMessage.setDisplayName(resources.getString("Properties_converterMessage_DisplayName"));
            prop_converterMessage.setShortDescription(resources.getString("Properties_converterMessage_Description"));
            prop_converterMessage.setPropertyEditorClass(com.sun.rave.propertyeditors.StringPropertyEditor.class);
            attrib = new AttributeDescriptor("converterMessage",false,null,true);
            prop_converterMessage.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_converterMessage.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            
            PropertyDescriptor prop_dir = new PropertyDescriptorBase("dir",beanClass,"getDir","setDir");
            prop_dir.setDisplayName(resources.getString("Properties_dir_DisplayName"));
            prop_dir.setShortDescription(resources.getString("Properties_dir_Description"));
            prop_dir.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            attrib = new AttributeDescriptor("dir",false,null,true);
            prop_dir.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dir.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_dir.setValue(com.sun.rave.propertyeditors.DomainPropertyEditor.DOMAIN_CLASS, com.sun.rave.propertyeditors.domains.TextDirectionDomain.class);

            PropertyDescriptor prop_label = new PropertyDescriptorBase("label", beanClass, "getLabel", "setLabel");
            prop_label.setDisplayName(resources.getString("Properties_label_DisplayName"));
            prop_label.setShortDescription(resources.getString("Properties_label_Description"));
            attrib = new AttributeDescriptor("label", false, null, true);
            prop_label.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib);
            prop_label.setValue(Constants.PropertyDescriptor.CATEGORY, CategoryDescriptors.APPEARANCE);
            
            PropertyDescriptor prop_lang = new PropertyDescriptorBase("lang",beanClass,"getLang","setLang");
            prop_lang.setDisplayName(resources.getString("Properties_lang_DisplayName"));
            prop_lang.setShortDescription(resources.getString("Properties_lang_Description"));
            prop_lang.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            attrib = new AttributeDescriptor("lang",false,null,true);
            prop_lang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_lang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_lang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_requiredMessage = new PropertyDescriptorBase("requiredMessage",beanClass,"getRequiredMessage","setRequiredMessage");
            prop_requiredMessage.setDisplayName(resources.getString("Properties_requiredMessage_DisplayName"));
            prop_requiredMessage.setShortDescription(resources.getString("Properties_requiredMessage_Description"));
            prop_requiredMessage.setPropertyEditorClass(com.sun.rave.propertyeditors.StringPropertyEditor.class);
            attrib = new AttributeDescriptor("requiredMessage",false,null,true);
            prop_requiredMessage.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_requiredMessage.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_validatorMessage = new PropertyDescriptorBase("validatorMessage",beanClass,"getValidatorMessage","setValidatorMessage");
            prop_validatorMessage.setDisplayName(resources.getString("Properties_validatorMessage_DisplayName"));
            prop_validatorMessage.setShortDescription(resources.getString("Properties_validatorMessage_Description"));
            prop_validatorMessage.setPropertyEditorClass(com.sun.rave.propertyeditors.StringPropertyEditor.class);
            attrib = new AttributeDescriptor("validatorMessage",false,null,true);
            prop_validatorMessage.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_validatorMessage.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
            propertyList.add(prop_converterMessage);
            propertyList.add(prop_dir);
            propertyList.add(prop_label);
            propertyList.add(prop_lang);
            propertyList.add(prop_requiredMessage);
            propertyList.add(prop_validatorMessage);
            return propertyList;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
        
    }
    
    
    
    static List<PropertyDescriptor> getMouseEventPropertyList(Class beanClass) {
        
        try {
            AttributeDescriptor attrib;
            PropertyDescriptor prop_onmousedown = new PropertyDescriptorBase("onmousedown",beanClass,"getOnmousedown","setOnmousedown");
            prop_onmousedown.setDisplayName(resources.getString("Properties_onmousedown_DisplayName"));
            prop_onmousedown.setShortDescription(resources.getString("Properties_onmousedown_Description"));
            prop_onmousedown.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            prop_onmousedown.setExpert(false);
            prop_onmousedown.setHidden(false);
            prop_onmousedown.setPreferred(false);
            attrib = new AttributeDescriptor("onmousedown",false,null,true);
            prop_onmousedown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onmousedown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onmousemove = new PropertyDescriptorBase("onmousemove",beanClass,"getOnmousemove","setOnmousemove");
            prop_onmousemove.setDisplayName(resources.getString("Properties_onmousemove_DisplayName"));
            prop_onmousemove.setShortDescription(resources.getString("Properties_onmousemove_Description"));
            prop_onmousemove.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onmousemove",false,null,true);
            prop_onmousemove.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onmousemove.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onmouseout = new PropertyDescriptorBase("onmouseout",beanClass,"getOnmouseout","setOnmouseout");
            prop_onmouseout.setDisplayName(resources.getString("Properties_onmouseout_DisplayName"));
            prop_onmouseout.setShortDescription(resources.getString("Properties_onmouseout_Description"));
            prop_onmouseout.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onmouseout",false,null,true);
            prop_onmouseout.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onmouseout.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onmouseover = new PropertyDescriptorBase("onmouseover",beanClass,"getOnmouseover","setOnmouseover");
            prop_onmouseover.setDisplayName(resources.getString("Properties_onmouseover_DisplayName"));
            prop_onmouseover.setShortDescription(resources.getString("Properties_onmouseover_Description"));
            prop_onmouseover.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onmouseover",false,null,true);
            prop_onmouseover.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onmouseover.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onmouseup = new PropertyDescriptorBase("onmouseup",beanClass,"getOnmouseup","setOnmouseup");
            prop_onmouseup.setDisplayName(resources.getString("Properties_onmouseup_DisplayName"));
            prop_onmouseup.setShortDescription(resources.getString("Properties_onmouseup_Description"));
            prop_onmouseup.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onmouseup",false,null,true);
            prop_onmouseup.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onmouseup.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
            propertyList.add(prop_onmouseup);
            propertyList.add(prop_onmousedown);
            propertyList.add(prop_onmouseover);
            propertyList.add(prop_onmouseout);
            propertyList.add(prop_onmousemove);
            return propertyList;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
        
    }
    
    
    static List<PropertyDescriptor> getKeyEventPropertyList(Class beanClass) {
        
        try {
            AttributeDescriptor attrib;
            
            PropertyDescriptor prop_onkeydown = new PropertyDescriptorBase("onkeydown",beanClass,"getOnkeydown","setOnkeydown");
            prop_onkeydown.setDisplayName(resources.getString("Properties_onkeydown_DisplayName"));
            prop_onkeydown.setShortDescription(resources.getString("Properties_onkeydown_Description"));
            prop_onkeydown.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onkeydown",false,null,true);
            prop_onkeydown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onkeydown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onkeypress = new PropertyDescriptorBase("onkeypress",beanClass,"getOnkeypress","setOnkeypress");
            prop_onkeypress.setDisplayName(resources.getString("Properties_onkeypress_DisplayName"));
            prop_onkeypress.setShortDescription(resources.getString("Properties_onkeypress_Description"));
            prop_onkeypress.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onkeypress",false,null,true);
            prop_onkeypress.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onkeypress.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onkeyup = new PropertyDescriptorBase("onkeyup",beanClass,"getOnkeyup","setOnkeyup");
            prop_onkeyup.setDisplayName(resources.getString("Properties_onkeyup_DisplayName"));
            prop_onkeyup.setShortDescription(resources.getString("Properties_onkeyup_Description"));
            prop_onkeyup.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onkeyup",false,null,true);
            prop_onkeyup.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onkeyup.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
            propertyList.add(prop_onkeydown);
            propertyList.add(prop_onkeypress);
            propertyList.add(prop_onkeyup);
            return propertyList;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
        
    }
    
    static List<PropertyDescriptor> getClickEventPropertyList(Class beanClass) {
        
        try {
            AttributeDescriptor attrib;
            
            PropertyDescriptor prop_onclick = new PropertyDescriptorBase("onclick",beanClass,"getOnclick","setOnclick");
            prop_onclick.setDisplayName(resources.getString("Properties_onclick_DisplayName"));
            prop_onclick.setShortDescription(resources.getString("Properties_onclick_Description"));
            prop_onclick.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onclick",false,null,true);
            prop_onclick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onclick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_ondblclick = new PropertyDescriptorBase("ondblclick",beanClass,"getOndblclick","setOndblclick");
            prop_ondblclick.setDisplayName(resources.getString("Properties_ondblclick_DisplayName"));
            prop_ondblclick.setShortDescription(resources.getString("Properties_ondblclick_Description"));
            prop_ondblclick.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("ondblclick",false,null,true);
            prop_ondblclick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_ondblclick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
            propertyList.add(prop_onclick);
            propertyList.add(prop_ondblclick);
            return propertyList;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
        
    }
    
    static List<PropertyDescriptor> getFocusEventPropertyList(Class beanClass) {
        
        try {
            AttributeDescriptor attrib;
            
            PropertyDescriptor prop_onblur = new PropertyDescriptorBase("onblur",beanClass,"getOnblur","setOnblur");
            prop_onblur.setDisplayName(resources.getString("Properties_onblur_DisplayName"));
            prop_onblur.setShortDescription(resources.getString("Properties_onblur_Description"));
            prop_onblur.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onblur",false,null,true);
            prop_onblur.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onblur.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onfocus = new PropertyDescriptorBase("onfocus",beanClass,"getOnfocus","setOnfocus");
            prop_onfocus.setDisplayName(resources.getString("Properties_onfocus_DisplayName"));
            prop_onfocus.setShortDescription(resources.getString("Properties_onfocus_Description"));
            prop_onfocus.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onfocus",false,null,true);
            prop_onfocus.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onfocus.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
            propertyList.add(prop_onfocus);
            propertyList.add(prop_onblur);
            return propertyList;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
        
    }
    
    static List<PropertyDescriptor> getSelectEventPropertyList(Class beanClass) {
        
        try {
            AttributeDescriptor attrib;
            
            PropertyDescriptor prop_onselect = new PropertyDescriptorBase("onselect",beanClass,"getOnselect","setOnselect");
            prop_onselect.setDisplayName(resources.getString("Properties_onselect_DisplayName"));
            prop_onselect.setShortDescription(resources.getString("Properties_onselect_Description"));
            prop_onselect.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onselect",false,null,true);
            prop_onselect.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onselect.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            
            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
            propertyList.add(prop_onselect);
            return propertyList;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
        
    }
    
    static List<PropertyDescriptor> getChangeEventPropertyList(Class beanClass) {
        
        try {
            AttributeDescriptor attrib;
            
            PropertyDescriptor prop_onchange = new PropertyDescriptorBase("onchange",beanClass,"getOnchange","setOnchange");
            prop_onchange.setDisplayName(resources.getString("Properties_onchange_DisplayName"));
            prop_onchange.setShortDescription(resources.getString("Properties_onchange_Description"));
            prop_onchange.setPropertyEditorClass(com.sun.rave.propertyeditors.JavaScriptPropertyEditor.class);
            attrib = new AttributeDescriptor("onchange",false,null,true);
            prop_onchange.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onchange.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            
            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();
            propertyList.add(prop_onchange);
            return propertyList;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
        
    }
}
