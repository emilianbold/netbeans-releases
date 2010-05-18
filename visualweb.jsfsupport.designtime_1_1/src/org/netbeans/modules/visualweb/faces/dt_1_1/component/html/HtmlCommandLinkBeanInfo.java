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

import org.netbeans.modules.visualweb.faces.dt_1_1.component.UICommandBeanInfoBase;
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

/**
 * The HtmlCommandHyperlinkBeanInfo class provides design-time meta data
 * for the HtmlCommandHyperlink component for use in a visual design tool.
 */
public class HtmlCommandLinkBeanInfo extends UICommandBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_1.component.html.Bundle-JSF", Locale.getDefault(), HtmlCommandLinkBeanInfo.class.getClassLoader());

    public HtmlCommandLinkBeanInfo() {
        beanClass = javax.faces.component.html.HtmlCommandLink.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlCommandLink_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlCommandLink_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlCommandLink_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/html/HtmlCommandLink_M32";
    }

    private BeanDescriptor beanDescriptor;

    /**
     * <p>Return the <code>BeanDescriptor</code> for this bean.</p>
     */
    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptorBase(beanClass);
        beanDescriptor.setDisplayName(resources.getString("HtmlCommandLink_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("HtmlCommandLink_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_jsfstd_link_action");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"linkAction");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_propsheets_jsfstd_link_action_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"commandLink");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_PREFIX,"h");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI,"http://java.sun.com/jsf/html");

        return beanDescriptor;

    }


    private PropertyDescriptor[] propertyDescriptors;

    /**
     * <p>Return the <code>PropertyDescriptor</code>s for this bean.</p>
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_accesskey = new PropertyDescriptorBase("accesskey",beanClass,"getAccesskey","setAccesskey");
            prop_accesskey.setDisplayName(resources.getString("HtmlCommandLink_accesskey_DisplayName"));
            prop_accesskey.setShortDescription(resources.getString("HtmlCommandLink_accesskey_Description"));
            prop_accesskey.setExpert(false);
            prop_accesskey.setHidden(false);
            prop_accesskey.setPreferred(false);
            attrib = new AttributeDescriptor("accesskey",false,null,true);
            prop_accesskey.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_accesskey.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_charset = new PropertyDescriptorBase("charset",beanClass,"getCharset","setCharset");
            prop_charset.setDisplayName(resources.getString("HtmlOutputLink_charset_DisplayName"));
            prop_charset.setShortDescription(resources.getString("HtmlOutputLink_charset_Description"));
            prop_charset.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_charset.setExpert(false);
            prop_charset.setHidden(false);
            prop_charset.setPreferred(false);
            attrib = new AttributeDescriptor("charset",false,null,true);
            prop_charset.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_charset.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_charset.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.CharacterSetsDomain.class);

            PropertyDescriptor prop_coords = new PropertyDescriptorBase("coords",beanClass,"getCoords","setCoords");
            prop_coords.setDisplayName(resources.getString("HtmlOutputLink_coords_DisplayName"));
            prop_coords.setShortDescription(resources.getString("HtmlOutputLink_coords_Description"));
            prop_coords.setPropertyEditorClass(com.sun.rave.propertyeditors.StringPropertyEditor.class);
            prop_coords.setExpert(false);
            prop_coords.setHidden(false);
            prop_coords.setPreferred(false);
            attrib = new AttributeDescriptor("coords",false,null,true);
            prop_coords.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_coords.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_dir = new PropertyDescriptorBase("dir",beanClass,"getDir","setDir");
            prop_dir.setDisplayName(resources.getString("HtmlCommandLink_dir_DisplayName"));
            prop_dir.setShortDescription(resources.getString("HtmlCommandLink_dir_Description"));
            prop_dir.setPropertyEditorClass(SelectOneDomainEditor.class);
            prop_dir.setExpert(false);
            prop_dir.setHidden(false);
            prop_dir.setPreferred(false);
            attrib = new AttributeDescriptor("dir",false,null,true);
            prop_dir.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dir.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_dir.setValue(DomainPropertyEditor.DOMAIN_CLASS, TextDirectionDomain.class);

            PropertyDescriptor prop_hreflang = new PropertyDescriptorBase("hreflang",beanClass,"getHreflang","setHreflang");
            prop_hreflang.setDisplayName(resources.getString("HtmlCommandLink_hreflang_DisplayName"));
            prop_hreflang.setShortDescription(resources.getString("HtmlCommandLink_hreflang_Description"));
            prop_hreflang.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_hreflang.setExpert(false);
            prop_hreflang.setHidden(false);
            prop_hreflang.setPreferred(false);
            attrib = new AttributeDescriptor("hreflang",false,null,true);
            prop_hreflang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_hreflang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_hreflang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_lang = new PropertyDescriptorBase("lang",beanClass,"getLang","setLang");
            prop_lang.setDisplayName(resources.getString("HtmlCommandLink_lang_DisplayName"));
            prop_lang.setShortDescription(resources.getString("HtmlCommandLink_lang_Description"));
            prop_lang.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_lang.setExpert(false);
            prop_lang.setHidden(false);
            prop_lang.setPreferred(false);
            attrib = new AttributeDescriptor("lang",false,null,true);
            prop_lang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_lang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_lang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_rel = new PropertyDescriptorBase("rel",beanClass,"getRel","setRel");
            prop_rel.setDisplayName(resources.getString("HtmlCommandLink_rel_DisplayName"));
            prop_rel.setShortDescription(resources.getString("HtmlCommandLink_rel_Description"));
            prop_rel.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_rel.setExpert(false);
            prop_rel.setHidden(false);
            prop_rel.setPreferred(false);
            attrib = new AttributeDescriptor("rel",false,null,true);
            prop_rel.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rel.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_rel.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlLinkTypesDomain.class);

            PropertyDescriptor prop_rev = new PropertyDescriptorBase("rev",beanClass,"getRev","setRev");
            prop_rev.setDisplayName(resources.getString("HtmlCommandLink_rev_DisplayName"));
            prop_rev.setShortDescription(resources.getString("HtmlCommandLink_rev_Description"));
            prop_rev.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_rev.setExpert(false);
            prop_rev.setHidden(false);
            prop_rev.setPreferred(false);
            attrib = new AttributeDescriptor("rev",false,null,true);
            prop_rev.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rev.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_rev.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlLinkTypesDomain.class);

            PropertyDescriptor prop_shape = new PropertyDescriptorBase("shape",beanClass,"getShape","setShape");
            prop_shape.setDisplayName(resources.getString("HtmlCommandLink_shape_DisplayName"));
            prop_shape.setShortDescription(resources.getString("HtmlCommandLink_shape_Description"));
            prop_shape.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_shape.setExpert(false);
            prop_shape.setHidden(false);
            prop_shape.setPreferred(false);
            attrib = new AttributeDescriptor("shape",false,null,true);
            prop_shape.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_shape.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_shape.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlRegionShapesDomain.class);

            PropertyDescriptor prop_tabindex = new PropertyDescriptorBase("tabindex",beanClass,"getTabindex","setTabindex");
            prop_tabindex.setDisplayName(resources.getString("HtmlCommandLink_tabindex_DisplayName"));
            prop_tabindex.setShortDescription(resources.getString("HtmlCommandLink_tabindex_Description"));
            prop_tabindex.setPropertyEditorClass(com.sun.rave.propertyeditors.IntegerPropertyEditor.class);
            prop_tabindex.setExpert(false);
            prop_tabindex.setHidden(false);
            prop_tabindex.setPreferred(false);
            attrib = new AttributeDescriptor("tabindex",false,null,true);
            prop_tabindex.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tabindex.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_tabindex.setValue("maxValue", new Integer(Short.MAX_VALUE));
            prop_tabindex.setValue("minValue", new Integer(0));

            PropertyDescriptor prop_target = new PropertyDescriptorBase("target",beanClass,"getTarget","setTarget");
            prop_target.setDisplayName(resources.getString("HtmlCommandLink_target_DisplayName"));
            prop_target.setShortDescription(resources.getString("HtmlCommandLink_target_Description"));
            prop_target.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_target.setExpert(false);
            prop_target.setHidden(false);
            prop_target.setPreferred(false);
            attrib = new AttributeDescriptor("target",false,null,true);
            prop_target.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_target.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);
            prop_target.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlFrameTargetsDomain.class);

            PropertyDescriptor prop_title = new PropertyDescriptorBase("title",beanClass,"getTitle","setTitle");
            prop_title.setDisplayName(resources.getString("HtmlCommandLink_title_DisplayName"));
            prop_title.setShortDescription(resources.getString("HtmlCommandLink_title_Description"));
            prop_title.setExpert(false);
            prop_title.setHidden(false);
            prop_title.setPreferred(false);
            attrib = new AttributeDescriptor("title",false,null,true);
            prop_title.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_title.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_type = new PropertyDescriptorBase("type",beanClass,"getType","setType");
            prop_type.setDisplayName(resources.getString("HtmlCommandLink_type_DisplayName"));
            prop_type.setShortDescription(resources.getString("HtmlCommandLink_type_Description"));
            prop_type.setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);
            prop_type.setExpert(false);
            prop_type.setHidden(false);
            prop_type.setPreferred(false);
            attrib = new AttributeDescriptor("type",false,null,true);
            prop_type.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_type.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_type.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.MimeTypesDomain.class);

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_accesskey);
            propertyDescriptorList.add(prop_charset);
            propertyDescriptorList.add(prop_coords);
            propertyDescriptorList.add(prop_dir);
            propertyDescriptorList.add(prop_hreflang);
            propertyDescriptorList.add(prop_lang);
            propertyDescriptorList.add(prop_rel);
            propertyDescriptorList.add(prop_rev);
            propertyDescriptorList.add(prop_shape);
            propertyDescriptorList.add(prop_tabindex);
            propertyDescriptorList.add(prop_target);
            propertyDescriptorList.add(prop_title);
            propertyDescriptorList.add(prop_type);

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

